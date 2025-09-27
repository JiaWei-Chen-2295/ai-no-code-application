package fun.javierchen.ainocodeapplication.core;

import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.constant.CodeFileConstant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeFileSaverTest {

    @Test
    void testSaveSingletonHtmlCode() {
        // 创建一个HtmlCodeResult对象，用于测试
        HtmlCodeResult result = new HtmlCodeResult();
        result.setHtmlCode("""
                <!DOCTYPE html>
                        <html lang="zh">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>用户登录</title>
                            <style>
                                body { font-family: Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
                                .login-container { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
                                h2 { text-align: center; color: #333; margin-bottom: 1.5rem; }
                                input { width: 100%; padding: 0.8rem; margin: 0.5rem 0 1rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
                                button { width: 100%; padding: 0.8rem; background: #1877f2; color: white; border: none; border-radius: 4px; cursor: pointer; }
                                button:hover { background: #166fe5; }
                            </style>
                        </head>
                        <body>
                            <div class="login-container">
                                <h2>用户登录</h2>
                                <form id="loginForm">
                                    <input type="text" id="username" placeholder="用户名" required>
                                    <input type="password" id="password" placeholder="密码" required>
                                    <button type="submit">登录</button>
                                </form>
                                <script>
                                    document.getElementById('loginForm').addEventListener('submit', function(e) {
                                        e.preventDefault();
                                        const user = document.getElementById('username').value;
                                        const pass = document.getElementById('password').value;
                                        alert(`登录信息:\\n用户名: ${user}\\n密码: ${pass}`);
                                    });
                                </script>
                            </div>
                        </body>
                        </html>
                """);
        CodeFileSaver.saveSingletonHtmlCode(result);
    }

    @Test
    void testSaveMutiFileCode() {
        MultiFileCodeResult result = new MultiFileCodeResult();
        result.setHtmlCode("""
                <!DOCTYPE html>
                <html lang="zh">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>用户登录</title>
                    <style>
                        body { font-family: Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
                        .login-container { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
                        h2 { text-align: center; color: #333; margin-bottom: 1.5rem; }
                        input { width: 100%; padding: 0.8rem; margin: 0.5rem 0 1rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
                        button { width: 100%; padding: 0.8rem; background: #1877f2; color: white; border: none; border-radius: 4px; cursor: pointer; }
                        button:hover { background: #166fe5; }
                    </style>
                </head>
                <body>
                    <div class="login-container">
                        <h2>用户登录</h2>
                        <form id="loginForm">
                            <input type="text" id="username" placeholder="用户名" required>
                            <input type="password" id="password" placeholder="密码" required>
                            <button type="submit">登录</button>
                        </form>
                        <script>
                            document.getElementById('loginForm').addEventListener('submit', function(e) {
                                e.preventDefault();
                                const user = document.getElementById('username').value;
                                const pass = document.getElementById('password').value;
                                alert(`登录信息:\\n用户名: ${user}\\n密码: ${pass}`);
                            });
                        </script>
                    </div>
                </body>
                </html>
                """);
        result.setCssCode("""
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f0f0f0;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                }
                
                .login-container {
                    background-color: white;
                    padding: 2rem;
                    border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                    width: 100%;
                    max-width: 400px;
                }
                
                h2 {
                    text-align: center;
                    margin-bottom: 1.5rem;
                }
                
                .input-group {
                    margin-bottom: 1rem;
                }
                
                label {
                    display: block;
                    margin-bottom: 0.5rem;
                }
                
                input {
                    width: 100%;
                    padding: 0.5rem;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    box-sizing: border-box;
                }
                
                button {
                    width: 100%;
                    padding: 0.75rem;
                    background-color: #007bff;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                }
                
                button:hover {
                    background-color: #0056b3;
                }
                
                #message {
                    margin-top: 1rem;
                    text-align: center;
                    color: #dc3545;
                }
                """);
        result.setJsCode("""
                // 获取表单和消息元素
                const loginForm = document.getElementById('loginForm');
                const messageDiv = document.getElementById('message');
                
                // 添加表单提交事件监听器
                loginForm.addEventListener('submit', function(e) {
                    // 阻止表单默认提交行为
                    e.preventDefault();
                
                    // 获取用户名和密码输入值
                    const username = document.getElementById('username').value;
                    const password = document.getElementById('password').value;
                
                    // 简单验证（实际项目中需要后端验证）
                    if (username === 'admin' && password === 'password') {
                        messageDiv.style.color = 'green';
                        messageDiv.textContent = '登录成功!';
                    } else {
                        messageDiv.style.color = 'red';
                        messageDiv.textContent = '用户名或密码错误!';
                    }
                });
                """);
        CodeFileSaver.saveMutiFileCode(result);

    }


}