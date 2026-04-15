/**
 * Element Picker — iframe 元素选取工具
 *
 * 注入到 preview iframe：
 * - hover 显示开发者工具风格的元素边界高亮（半透明蓝色覆盖层 + 边框 + 标签名 + 尺寸）
 * - 阻止 mousedown / mouseup / pointerdown / click 等事件（capture 阶段 stopImmediatePropagation）
 * - 点击捕获 CSS 选择器 + outerHTML/组件路径，通过 postMessage 发送给父窗口
 *
 * 用法：
 *   import { enableElementPicker, disableElementPicker } from '@/utils/elementPicker'
 *   enableElementPicker(iframeElement, origin)
 *   disableElementPicker(iframeElement)
 */

export interface ElementPickedPayload {
  type: 'elementPicked'
  cssSelector: string
  elementContext: string
  tagName: string
}

/** 注入到 iframe contentDocument 的代码字符串 */
function buildInjectionScript(parentOrigin: string): string {
  return `
(function() {
  if (window.__elementPickerActive) return;
  window.__elementPickerActive = true;

  // ── 创建覆盖层（pointer-events:none，不干扰鼠标事件路由）──
  var overlay = document.createElement('div');
  overlay.id = '__ep_overlay';
  overlay.style.cssText = [
    'position:fixed',
    'pointer-events:none',
    'z-index:2147483646',
    'background:rgba(99,102,241,0.15)',
    'border:2px solid rgba(99,102,241,0.85)',
    'box-sizing:border-box',
    'display:none'
  ].join(';');
  document.documentElement.appendChild(overlay);

  // 标签名徽章（显示 tagName + id/class）
  var badge = document.createElement('div');
  badge.id = '__ep_badge';
  badge.style.cssText = [
    'position:fixed',
    'pointer-events:none',
    'z-index:2147483647',
    'background:#6366f1',
    'color:#fff',
    'font:bold 11px/1 monospace',
    'padding:3px 7px',
    'border-radius:3px 3px 3px 0',
    'display:none',
    'white-space:nowrap',
    'max-width:260px',
    'overflow:hidden',
    'text-overflow:ellipsis'
  ].join(';');
  document.documentElement.appendChild(badge);

  // 尺寸提示
  var sizeBadge = document.createElement('div');
  sizeBadge.id = '__ep_size';
  sizeBadge.style.cssText = [
    'position:fixed',
    'pointer-events:none',
    'z-index:2147483647',
    'background:rgba(30,30,30,0.75)',
    'color:#e0e0e0',
    'font:11px/1 monospace',
    'padding:3px 7px',
    'border-radius:3px',
    'display:none',
    'white-space:nowrap'
  ].join(';');
  document.documentElement.appendChild(sizeBadge);

  // 全局 crosshair 光标
  var styleEl = document.createElement('style');
  styleEl.id = '__ep_style';
  styleEl.textContent = '*{cursor:crosshair!important}';
  (document.head || document.documentElement).appendChild(styleEl);

  var currentEl = null;

  function cssescape(str) {
    if (typeof CSS !== 'undefined' && CSS.escape) return CSS.escape(str);
    return str.replace(/([\0-\x1f\x7f]|^-?\d|^-$|[^\x80-\uFFFF\w-])/g, '\\$1');
  }

  function buildSelector(el) {
    if (!el || el === document.documentElement) return 'html';
    var parts = [];
    var current = el;
    while (current && current !== document.documentElement && current.nodeType === 1) {
      var part = current.tagName.toLowerCase();
      if (current.id) {
        part += '#' + cssescape(current.id);
        parts.unshift(part);
        break;
      }
      var classes = Array.from(current.classList).filter(function(c){ return c.trim(); });
      if (classes.length) {
        part += '.' + classes.map(cssescape).join('.');
      }
      var parent = current.parentElement;
      if (parent) {
        var siblings = Array.from(parent.children).filter(function(c){
          return c.tagName === current.tagName;
        });
        if (siblings.length > 1) {
          part += ':nth-of-type(' + (siblings.indexOf(current) + 1) + ')';
        }
      }
      parts.unshift(part);
      current = current.parentElement;
    }
    return parts.join(' > ') || el.tagName.toLowerCase();
  }

  function getElementContext(el) {
    if (el.__vueParentComponent) {
      var comp = el.__vueParentComponent;
      var file = comp.type && comp.type.__file ? comp.type.__file : '';
      if (file) return 'Vue component: ' + file;
    }
    var html = el.outerHTML || '';
    return html.length > 2000 ? html.substring(0, 2000) + '...' : html;
  }

  function showOverlay(el) {
    if (!el || el === document.documentElement || el === document.body) {
      hideOverlay(); return;
    }
    var r = el.getBoundingClientRect();
    overlay.style.left   = r.left   + 'px';
    overlay.style.top    = r.top    + 'px';
    overlay.style.width  = r.width  + 'px';
    overlay.style.height = r.height + 'px';
    overlay.style.display = 'block';

    var text = el.tagName.toLowerCase();
    if (el.id) {
      text += '#' + el.id;
    } else if (el.className && typeof el.className === 'string') {
      var firstCls = el.className.trim().split(/\s+/).slice(0, 2).join('.');
      if (firstCls) text += '.' + firstCls;
    }
    badge.textContent = text;
    sizeBadge.textContent = Math.round(r.width) + ' \u00d7 ' + Math.round(r.height);

    var badgeTop = r.top - 22;
    if (badgeTop < 0) badgeTop = r.bottom + 2;
    badgeTop = Math.max(0, badgeTop);
    var badgeLeft = Math.max(0, r.left);

    badge.style.top    = badgeTop + 'px';
    badge.style.left   = badgeLeft + 'px';
    badge.style.display = 'block';

    sizeBadge.style.top  = badgeTop + 'px';
    sizeBadge.style.left = (badgeLeft + badge.offsetWidth + 4) + 'px';
    sizeBadge.style.display = 'block';
  }

  function hideOverlay() {
    overlay.style.display   = 'none';
    badge.style.display     = 'none';
    sizeBadge.style.display = 'none';
    currentEl = null;
  }

  // ── 事件处理 ──
  function onMouseMove(e) {
    // 用坐标命中测试，避免覆盖层（pointer-events:none）干扰
    var el = document.elementFromPoint(e.clientX, e.clientY);
    if (!el || el === currentEl || el.id === '__ep_overlay' || el.id === '__ep_badge' || el.id === '__ep_size') return;
    currentEl = el;
    showOverlay(el);
  }

  // 拦截所有可能触发 UI 副作用的鼠标事件
  var blockHandler = function(e) {
    e.preventDefault();
    e.stopImmediatePropagation();
  };

  function onClick(e) {
    e.preventDefault();
    e.stopImmediatePropagation();
    var el = document.elementFromPoint(e.clientX, e.clientY);
    if (!el || el.id === '__ep_overlay') return;
    var cssSelector   = buildSelector(el);
    var elementContext = getElementContext(el);
    hideOverlay();
    window.parent.postMessage({
      type: 'elementPicked',
      cssSelector: cssSelector,
      elementContext: elementContext,
      tagName: el.tagName.toLowerCase()
    }, '${parentOrigin}');
  }

  document.addEventListener('mousemove',   onMouseMove,   true);
  document.addEventListener('click',        onClick,       true);
  document.addEventListener('mousedown',    blockHandler,  true);
  document.addEventListener('mouseup',      blockHandler,  true);
  document.addEventListener('pointerdown',  blockHandler,  true);
  document.addEventListener('pointerup',    blockHandler,  true);
  document.addEventListener('contextmenu',  blockHandler,  true);

  window.__elementPickerCleanup = function() {
    document.removeEventListener('mousemove',   onMouseMove,  true);
    document.removeEventListener('click',        onClick,      true);
    document.removeEventListener('mousedown',    blockHandler, true);
    document.removeEventListener('mouseup',      blockHandler, true);
    document.removeEventListener('pointerdown',  blockHandler, true);
    document.removeEventListener('pointerup',    blockHandler, true);
    document.removeEventListener('contextmenu',  blockHandler, true);
    overlay.remove();
    badge.remove();
    sizeBadge.remove();
    var s = document.getElementById('__ep_style');
    if (s) s.remove();
    window.__elementPickerActive = false;
    delete window.__elementPickerCleanup;
  };
})();
  `.trim()
}

/**
 * 在 iframe 中启用元素选取模式
 * @param iframe  预览用的 iframe 元素
 * @param origin  父窗口 origin（用于 postMessage 安全校验），留空时使用 location.origin
 */
export function enableElementPicker(
  iframe: HTMLIFrameElement,
  origin: string = window.location.origin
): boolean {
  let doc: Document | null = null
  try {
    doc = iframe.contentDocument
  } catch {
    // cross-origin — contentDocument access denied
  }
  if (!doc) {
    console.error(
      '[ElementPicker] Cannot access iframe contentDocument. ' +
      'The preview iframe must be same-origin. ' +
      'Current src:', iframe.src
    )
    return false
  }
  // 若已注入，先清理旧实例
  const win = iframe.contentWindow as (Window & { __elementPickerCleanup?: () => void }) | null
  if (win?.__elementPickerCleanup) {
    win.__elementPickerCleanup()
  }
  const script = doc.createElement('script')
  script.textContent = buildInjectionScript(origin)
  ;(doc.head ?? doc.documentElement).appendChild(script)
  script.remove()
  return true
}

/**
 * 在 iframe 中禁用元素选取模式
 */
export function disableElementPicker(iframe: HTMLIFrameElement): void {
  try {
    const win = iframe.contentWindow as (Window & { __elementPickerCleanup?: () => void }) | null
    if (win?.__elementPickerCleanup) {
      win.__elementPickerCleanup()
    }
  } catch {
    // cross-origin guard — silently ignore
  }
}
