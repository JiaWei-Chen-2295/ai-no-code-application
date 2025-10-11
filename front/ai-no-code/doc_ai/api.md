# OpenAPI definition


**简介**:OpenAPI definition


**HOST**:http://localhost:8081/api


**联系人**:


**Version**:v0


**接口路径**:/api/v3/api-docs/default


[TOC]






# app-controller


## getAppVersions


**接口地址**:`/api/app/{appId}/versions`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseListAppVersionVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||array|AppVersionVO|
|&emsp;&emsp;version||integer(int32)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;message||string||
|&emsp;&emsp;isDeployed||boolean||
|&emsp;&emsp;fileSize||integer(int64)||
|&emsp;&emsp;previewUrl||string||
|&emsp;&emsp;chatHistoryId||integer(int64)||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": [
		{
			"version": 0,
			"createTime": "",
			"message": "",
			"isDeployed": true,
			"fileSize": 0,
			"previewUrl": "",
			"chatHistoryId": 0
		}
	],
	"message": ""
}
```


## getAppVersion


**接口地址**:`/api/app/{appId}/versions/{version}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||path|true|integer(int64)||
|version||path|true|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseAppVersionVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||AppVersionVO|AppVersionVO|
|&emsp;&emsp;version||integer(int32)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;message||string||
|&emsp;&emsp;isDeployed||boolean||
|&emsp;&emsp;fileSize||integer(int64)||
|&emsp;&emsp;previewUrl||string||
|&emsp;&emsp;chatHistoryId||integer(int64)||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"version": 0,
		"createTime": "",
		"message": "",
		"isDeployed": true,
		"fileSize": 0,
		"previewUrl": "",
		"chatHistoryId": 0
	},
	"message": ""
}
```


## deleteAppVersion


**接口地址**:`/api/app/{appId}/versions/{version}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||path|true|integer(int64)||
|version||path|true|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


## addApp


**接口地址**:`/api/app/add`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "appName": "",
  "initPrompt": "",
  "codeGenType": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appAddRequest|AppAddRequest|body|true|AppAddRequest|AppAddRequest|
|&emsp;&emsp;appName|||false|string||
|&emsp;&emsp;initPrompt|||false|string||
|&emsp;&emsp;codeGenType|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseLong|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||integer(int64)|integer(int64)|
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": 0,
	"message": ""
}
```


## chatGenCode


**接口地址**:`/api/app/chat/gen/code`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`text/event-stream`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||query|true|integer(int64)||
|message||query|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|ServerSentEventString|


**响应参数**:


暂无


**响应示例**:
```javascript
[
	null
]
```


## deleteMyApp


**接口地址**:`/api/app/delete`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|deleteId||query|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


## deleteApp


**接口地址**:`/api/app/delete/admin`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|deleteId||query|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


## deployApp


**接口地址**:`/api/app/deploy`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "appId": 0,
  "version": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appDeployRequest|AppDeployRequest|body|true|AppDeployRequest|AppDeployRequest|
|&emsp;&emsp;appId|||false|integer(int64)||
|&emsp;&emsp;version|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseString|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||string||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": "",
	"message": ""
}
```


## listFeaturedAppVOByPage


**接口地址**:`/api/app/featured/list/page/vo`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "current": 0,
  "pageSize": 0,
  "sortField": "",
  "sortOrder": "",
  "id": 0,
  "appName": "",
  "codeGenType": "",
  "userId": 0,
  "priority": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appQueryRequest|AppQueryRequest|body|true|AppQueryRequest|AppQueryRequest|
|&emsp;&emsp;current|||false|integer(int32)||
|&emsp;&emsp;pageSize|||false|integer(int32)||
|&emsp;&emsp;sortField|||false|string||
|&emsp;&emsp;sortOrder|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;appName|||false|string||
|&emsp;&emsp;codeGenType|||false|string||
|&emsp;&emsp;userId|||false|integer(int64)||
|&emsp;&emsp;priority|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponsePageAppVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageAppVO|PageAppVO|
|&emsp;&emsp;records||array|AppVO|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;appName||string||
|&emsp;&emsp;&emsp;&emsp;cover||string||
|&emsp;&emsp;&emsp;&emsp;initPrompt||string||
|&emsp;&emsp;&emsp;&emsp;codeGenType||string||
|&emsp;&emsp;&emsp;&emsp;deployKey||string||
|&emsp;&emsp;&emsp;&emsp;deployedTime||string||
|&emsp;&emsp;&emsp;&emsp;priority||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;editTime||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;&emsp;&emsp;user||UserVO|UserVO|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;account||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;email||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;name||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;avatar||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;profile||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;role||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;pageNumber||integer(int64)||
|&emsp;&emsp;pageSize||integer(int64)||
|&emsp;&emsp;totalPage||integer(int64)||
|&emsp;&emsp;totalRow||integer(int64)||
|&emsp;&emsp;optimizeCountQuery||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [
			{
				"id": 0,
				"appName": "",
				"cover": "",
				"initPrompt": "",
				"codeGenType": "",
				"deployKey": "",
				"deployedTime": "",
				"priority": 0,
				"userId": 0,
				"editTime": "",
				"createTime": "",
				"updateTime": "",
				"user": {
					"id": 0,
					"account": "",
					"email": "",
					"name": "",
					"avatar": "",
					"profile": "",
					"role": "",
					"createTime": "",
					"updateTime": ""
				}
			}
		],
		"pageNumber": 0,
		"pageSize": 0,
		"totalPage": 0,
		"totalRow": 0,
		"optimizeCountQuery": true
	},
	"message": ""
}
```


## getAppById


**接口地址**:`/api/app/get/admin`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||query|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseApp|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||App|App|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;appName||string||
|&emsp;&emsp;cover||string||
|&emsp;&emsp;initPrompt||string||
|&emsp;&emsp;codeGenType||string||
|&emsp;&emsp;deployKey||string||
|&emsp;&emsp;deployedTime||string(date-time)||
|&emsp;&emsp;priority||integer(int32)||
|&emsp;&emsp;userId||integer(int64)||
|&emsp;&emsp;editTime||string(date-time)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|&emsp;&emsp;isDelete||integer(int32)||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"id": 0,
		"appName": "",
		"cover": "",
		"initPrompt": "",
		"codeGenType": "",
		"deployKey": "",
		"deployedTime": "",
		"priority": 0,
		"userId": 0,
		"editTime": "",
		"createTime": "",
		"updateTime": "",
		"isDelete": 0
	},
	"message": ""
}
```


## getAppVOById


**接口地址**:`/api/app/get/vo`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||query|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseAppVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||AppVO|AppVO|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;appName||string||
|&emsp;&emsp;cover||string||
|&emsp;&emsp;initPrompt||string||
|&emsp;&emsp;codeGenType||string||
|&emsp;&emsp;deployKey||string||
|&emsp;&emsp;deployedTime||string(date-time)||
|&emsp;&emsp;priority||integer(int32)||
|&emsp;&emsp;userId||integer(int64)||
|&emsp;&emsp;editTime||string(date-time)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|&emsp;&emsp;user||UserVO|UserVO|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;account||string||
|&emsp;&emsp;&emsp;&emsp;email||string||
|&emsp;&emsp;&emsp;&emsp;name||string||
|&emsp;&emsp;&emsp;&emsp;avatar||string||
|&emsp;&emsp;&emsp;&emsp;profile||string||
|&emsp;&emsp;&emsp;&emsp;role||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"id": 0,
		"appName": "",
		"cover": "",
		"initPrompt": "",
		"codeGenType": "",
		"deployKey": "",
		"deployedTime": "",
		"priority": 0,
		"userId": 0,
		"editTime": "",
		"createTime": "",
		"updateTime": "",
		"user": {
			"id": 0,
			"account": "",
			"email": "",
			"name": "",
			"avatar": "",
			"profile": "",
			"role": "",
			"createTime": "",
			"updateTime": ""
		}
	},
	"message": ""
}
```


## listAppByPage


**接口地址**:`/api/app/list/page/admin`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "current": 0,
  "pageSize": 0,
  "sortField": "",
  "sortOrder": "",
  "id": 0,
  "appName": "",
  "codeGenType": "",
  "userId": 0,
  "priority": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appQueryRequest|AppQueryRequest|body|true|AppQueryRequest|AppQueryRequest|
|&emsp;&emsp;current|||false|integer(int32)||
|&emsp;&emsp;pageSize|||false|integer(int32)||
|&emsp;&emsp;sortField|||false|string||
|&emsp;&emsp;sortOrder|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;appName|||false|string||
|&emsp;&emsp;codeGenType|||false|string||
|&emsp;&emsp;userId|||false|integer(int64)||
|&emsp;&emsp;priority|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponsePageApp|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageApp|PageApp|
|&emsp;&emsp;records||array|App|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;appName||string||
|&emsp;&emsp;&emsp;&emsp;cover||string||
|&emsp;&emsp;&emsp;&emsp;initPrompt||string||
|&emsp;&emsp;&emsp;&emsp;codeGenType||string||
|&emsp;&emsp;&emsp;&emsp;deployKey||string||
|&emsp;&emsp;&emsp;&emsp;deployedTime||string||
|&emsp;&emsp;&emsp;&emsp;priority||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;editTime||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;&emsp;&emsp;isDelete||integer||
|&emsp;&emsp;pageNumber||integer(int64)||
|&emsp;&emsp;pageSize||integer(int64)||
|&emsp;&emsp;totalPage||integer(int64)||
|&emsp;&emsp;totalRow||integer(int64)||
|&emsp;&emsp;optimizeCountQuery||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [
			{
				"id": 0,
				"appName": "",
				"cover": "",
				"initPrompt": "",
				"codeGenType": "",
				"deployKey": "",
				"deployedTime": "",
				"priority": 0,
				"userId": 0,
				"editTime": "",
				"createTime": "",
				"updateTime": "",
				"isDelete": 0
			}
		],
		"pageNumber": 0,
		"pageSize": 0,
		"totalPage": 0,
		"totalRow": 0,
		"optimizeCountQuery": true
	},
	"message": ""
}
```


## listMyAppVOByPage


**接口地址**:`/api/app/my/list/page/vo`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "current": 0,
  "pageSize": 0,
  "sortField": "",
  "sortOrder": "",
  "id": 0,
  "appName": "",
  "codeGenType": "",
  "userId": 0,
  "priority": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appQueryRequest|AppQueryRequest|body|true|AppQueryRequest|AppQueryRequest|
|&emsp;&emsp;current|||false|integer(int32)||
|&emsp;&emsp;pageSize|||false|integer(int32)||
|&emsp;&emsp;sortField|||false|string||
|&emsp;&emsp;sortOrder|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;appName|||false|string||
|&emsp;&emsp;codeGenType|||false|string||
|&emsp;&emsp;userId|||false|integer(int64)||
|&emsp;&emsp;priority|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponsePageAppVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageAppVO|PageAppVO|
|&emsp;&emsp;records||array|AppVO|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;appName||string||
|&emsp;&emsp;&emsp;&emsp;cover||string||
|&emsp;&emsp;&emsp;&emsp;initPrompt||string||
|&emsp;&emsp;&emsp;&emsp;codeGenType||string||
|&emsp;&emsp;&emsp;&emsp;deployKey||string||
|&emsp;&emsp;&emsp;&emsp;deployedTime||string||
|&emsp;&emsp;&emsp;&emsp;priority||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;editTime||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;&emsp;&emsp;user||UserVO|UserVO|
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;account||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;email||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;name||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;avatar||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;profile||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;role||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;pageNumber||integer(int64)||
|&emsp;&emsp;pageSize||integer(int64)||
|&emsp;&emsp;totalPage||integer(int64)||
|&emsp;&emsp;totalRow||integer(int64)||
|&emsp;&emsp;optimizeCountQuery||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [
			{
				"id": 0,
				"appName": "",
				"cover": "",
				"initPrompt": "",
				"codeGenType": "",
				"deployKey": "",
				"deployedTime": "",
				"priority": 0,
				"userId": 0,
				"editTime": "",
				"createTime": "",
				"updateTime": "",
				"user": {
					"id": 0,
					"account": "",
					"email": "",
					"name": "",
					"avatar": "",
					"profile": "",
					"role": "",
					"createTime": "",
					"updateTime": ""
				}
			}
		],
		"pageNumber": 0,
		"pageSize": 0,
		"totalPage": 0,
		"totalRow": 0,
		"optimizeCountQuery": true
	},
	"message": ""
}
```


## updateApp


**接口地址**:`/api/app/update/admin`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "appName": "",
  "cover": "",
  "priority": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appUpdateRequest|AppUpdateRequest|body|true|AppUpdateRequest|AppUpdateRequest|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;appName|||false|string||
|&emsp;&emsp;cover|||false|string||
|&emsp;&emsp;priority|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


## updateMyApp


**接口地址**:`/api/app/update/my`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "appName": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appUpdateMyRequest|AppUpdateMyRequest|body|true|AppUpdateMyRequest|AppUpdateMyRequest|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;appName|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


# chat-history-controller


## listAllChatHistoryByPageForAdmin


**接口地址**:`/api/chatHistory/admin/list/page/vo`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "current": 0,
  "pageSize": 0,
  "sortField": "",
  "sortOrder": "",
  "appId": 0,
  "message": "",
  "messageType": "",
  "userId": 0,
  "lastCreateTime": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|chatHistoryQueryRequest|ChatHistoryQueryRequest|body|true|ChatHistoryQueryRequest|ChatHistoryQueryRequest|
|&emsp;&emsp;current|||false|integer(int32)||
|&emsp;&emsp;pageSize|||false|integer(int32)||
|&emsp;&emsp;sortField|||false|string||
|&emsp;&emsp;sortOrder|||false|string||
|&emsp;&emsp;appId|||false|integer(int64)||
|&emsp;&emsp;message|||false|string||
|&emsp;&emsp;messageType|||false|string||
|&emsp;&emsp;userId|||false|integer(int64)||
|&emsp;&emsp;lastCreateTime|||false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponsePageChatHistory|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageChatHistory|PageChatHistory|
|&emsp;&emsp;records||array|ChatHistory|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;message||string||
|&emsp;&emsp;&emsp;&emsp;messageType||string||
|&emsp;&emsp;&emsp;&emsp;appId||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;isCode||integer||
|&emsp;&emsp;&emsp;&emsp;codeVersion||integer||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;&emsp;&emsp;isDelete||integer||
|&emsp;&emsp;pageNumber||integer(int64)||
|&emsp;&emsp;pageSize||integer(int64)||
|&emsp;&emsp;totalPage||integer(int64)||
|&emsp;&emsp;totalRow||integer(int64)||
|&emsp;&emsp;optimizeCountQuery||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [
			{
				"id": 0,
				"message": "",
				"messageType": "",
				"appId": 0,
				"userId": 0,
				"isCode": 0,
				"codeVersion": 0,
				"createTime": "",
				"updateTime": "",
				"isDelete": 0
			}
		],
		"pageNumber": 0,
		"pageSize": 0,
		"totalPage": 0,
		"totalRow": 0,
		"optimizeCountQuery": true
	},
	"message": ""
}
```


## listAppChatHistory


**接口地址**:`/api/chatHistory/app/{appId}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||path|true|integer(int64)||
|pageSize||query|false|integer(int32)||
|lastCreateTime||query|false|string(date-time)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponsePageChatHistory|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageChatHistory|PageChatHistory|
|&emsp;&emsp;records||array|ChatHistory|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;message||string||
|&emsp;&emsp;&emsp;&emsp;messageType||string||
|&emsp;&emsp;&emsp;&emsp;appId||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;isCode||integer||
|&emsp;&emsp;&emsp;&emsp;codeVersion||integer||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;&emsp;&emsp;isDelete||integer||
|&emsp;&emsp;pageNumber||integer(int64)||
|&emsp;&emsp;pageSize||integer(int64)||
|&emsp;&emsp;totalPage||integer(int64)||
|&emsp;&emsp;totalRow||integer(int64)||
|&emsp;&emsp;optimizeCountQuery||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [
			{
				"id": 0,
				"message": "",
				"messageType": "",
				"appId": 0,
				"userId": 0,
				"isCode": 0,
				"codeVersion": 0,
				"createTime": "",
				"updateTime": "",
				"isDelete": 0
			}
		],
		"pageNumber": 0,
		"pageSize": 0,
		"totalPage": 0,
		"totalRow": 0,
		"optimizeCountQuery": true
	},
	"message": ""
}
```


# health-controller


## health


**接口地址**:`/api/health`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK||


**响应参数**:


暂无


**响应示例**:
```javascript

```


# static-resource-controller


## serveStaticResource


**接口地址**:`/api/static/{deployKey}/**`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|deployKey||path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK||


**响应参数**:


暂无


**响应示例**:
```javascript

```


## previewSpecificVersion


**接口地址**:`/api/static/preview/{appId}/{version}/**`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||path|true|integer(int64)||
|version||path|true|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK||


**响应参数**:


暂无


**响应示例**:
```javascript

```


## previewLatestVersion


**接口地址**:`/api/static/preview/{appId}/**`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|appId||path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK||


**响应参数**:


暂无


**响应示例**:
```javascript

```


# user-controller


## addUser


**接口地址**:`/api/user/add`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "userName": "",
  "userAccount": "",
  "userAvatar": "",
  "email": "",
  "userRole": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userAddRequest|UserAddRequest|body|true|UserAddRequest|UserAddRequest|
|&emsp;&emsp;userName|||false|string||
|&emsp;&emsp;userAccount|||false|string||
|&emsp;&emsp;userAvatar|||false|string||
|&emsp;&emsp;email|||false|string||
|&emsp;&emsp;userRole|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseLong|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||integer(int64)|integer(int64)|
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": 0,
	"message": ""
}
```


## deleteUser


**接口地址**:`/api/user/delete`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|deleteId||query|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


## getUserById


**接口地址**:`/api/user/get`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||query|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseUser|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||User|User|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;account||string||
|&emsp;&emsp;password||string||
|&emsp;&emsp;email||string||
|&emsp;&emsp;name||string||
|&emsp;&emsp;avatar||string||
|&emsp;&emsp;profile||string||
|&emsp;&emsp;role||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|&emsp;&emsp;isDelete||integer(int32)||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"id": 0,
		"account": "",
		"password": "",
		"email": "",
		"name": "",
		"avatar": "",
		"profile": "",
		"role": "",
		"createTime": "",
		"updateTime": "",
		"isDelete": 0
	},
	"message": ""
}
```


## getLoginUser


**接口地址**:`/api/user/get/login`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseLoginUserVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||LoginUserVO|LoginUserVO|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;userName||string||
|&emsp;&emsp;userAvatar||string||
|&emsp;&emsp;userProfile||string||
|&emsp;&emsp;userRole||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"id": 0,
		"userName": "",
		"userAvatar": "",
		"userProfile": "",
		"userRole": "",
		"createTime": "",
		"updateTime": ""
	},
	"message": ""
}
```


## getUserVOById


**接口地址**:`/api/user/get/vo`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id||query|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseUserVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||UserVO|UserVO|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;account||string||
|&emsp;&emsp;email||string||
|&emsp;&emsp;name||string||
|&emsp;&emsp;avatar||string||
|&emsp;&emsp;profile||string||
|&emsp;&emsp;role||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"id": 0,
		"account": "",
		"email": "",
		"name": "",
		"avatar": "",
		"profile": "",
		"role": "",
		"createTime": "",
		"updateTime": ""
	},
	"message": ""
}
```


## listUserByPage


**接口地址**:`/api/user/list/page`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "current": 0,
  "pageSize": 0,
  "sortField": "",
  "sortOrder": "",
  "id": 0,
  "unionId": "",
  "mpOpenId": "",
  "userName": "",
  "userProfile": "",
  "userRole": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userQueryRequest|UserQueryRequest|body|true|UserQueryRequest|UserQueryRequest|
|&emsp;&emsp;current|||false|integer(int32)||
|&emsp;&emsp;pageSize|||false|integer(int32)||
|&emsp;&emsp;sortField|||false|string||
|&emsp;&emsp;sortOrder|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;unionId|||false|string||
|&emsp;&emsp;mpOpenId|||false|string||
|&emsp;&emsp;userName|||false|string||
|&emsp;&emsp;userProfile|||false|string||
|&emsp;&emsp;userRole|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponsePageUser|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageUser|PageUser|
|&emsp;&emsp;records||array|User|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;account||string||
|&emsp;&emsp;&emsp;&emsp;password||string||
|&emsp;&emsp;&emsp;&emsp;email||string||
|&emsp;&emsp;&emsp;&emsp;name||string||
|&emsp;&emsp;&emsp;&emsp;avatar||string||
|&emsp;&emsp;&emsp;&emsp;profile||string||
|&emsp;&emsp;&emsp;&emsp;role||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;&emsp;&emsp;isDelete||integer||
|&emsp;&emsp;pageNumber||integer(int64)||
|&emsp;&emsp;pageSize||integer(int64)||
|&emsp;&emsp;totalPage||integer(int64)||
|&emsp;&emsp;totalRow||integer(int64)||
|&emsp;&emsp;optimizeCountQuery||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [
			{
				"id": 0,
				"account": "",
				"password": "",
				"email": "",
				"name": "",
				"avatar": "",
				"profile": "",
				"role": "",
				"createTime": "",
				"updateTime": "",
				"isDelete": 0
			}
		],
		"pageNumber": 0,
		"pageSize": 0,
		"totalPage": 0,
		"totalRow": 0,
		"optimizeCountQuery": true
	},
	"message": ""
}
```


## listUserVOByPage


**接口地址**:`/api/user/list/page/vo`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "current": 0,
  "pageSize": 0,
  "sortField": "",
  "sortOrder": "",
  "id": 0,
  "unionId": "",
  "mpOpenId": "",
  "userName": "",
  "userProfile": "",
  "userRole": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userQueryRequest|UserQueryRequest|body|true|UserQueryRequest|UserQueryRequest|
|&emsp;&emsp;current|||false|integer(int32)||
|&emsp;&emsp;pageSize|||false|integer(int32)||
|&emsp;&emsp;sortField|||false|string||
|&emsp;&emsp;sortOrder|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;unionId|||false|string||
|&emsp;&emsp;mpOpenId|||false|string||
|&emsp;&emsp;userName|||false|string||
|&emsp;&emsp;userProfile|||false|string||
|&emsp;&emsp;userRole|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponsePageUserVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageUserVO|PageUserVO|
|&emsp;&emsp;records||array|UserVO|
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;account||string||
|&emsp;&emsp;&emsp;&emsp;email||string||
|&emsp;&emsp;&emsp;&emsp;name||string||
|&emsp;&emsp;&emsp;&emsp;avatar||string||
|&emsp;&emsp;&emsp;&emsp;profile||string||
|&emsp;&emsp;&emsp;&emsp;role||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;updateTime||string||
|&emsp;&emsp;pageNumber||integer(int64)||
|&emsp;&emsp;pageSize||integer(int64)||
|&emsp;&emsp;totalPage||integer(int64)||
|&emsp;&emsp;totalRow||integer(int64)||
|&emsp;&emsp;optimizeCountQuery||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [
			{
				"id": 0,
				"account": "",
				"email": "",
				"name": "",
				"avatar": "",
				"profile": "",
				"role": "",
				"createTime": "",
				"updateTime": ""
			}
		],
		"pageNumber": 0,
		"pageSize": 0,
		"totalPage": 0,
		"totalRow": 0,
		"optimizeCountQuery": true
	},
	"message": ""
}
```


## userLogin


**接口地址**:`/api/user/login`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "userAccount": "",
  "userPassword": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userLoginRequest|UserLoginRequest|body|true|UserLoginRequest|UserLoginRequest|
|&emsp;&emsp;userAccount|||false|string||
|&emsp;&emsp;userPassword|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseLoginUserVO|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||LoginUserVO|LoginUserVO|
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;userName||string||
|&emsp;&emsp;userAvatar||string||
|&emsp;&emsp;userProfile||string||
|&emsp;&emsp;userRole||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;updateTime||string(date-time)||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"id": 0,
		"userName": "",
		"userAvatar": "",
		"userProfile": "",
		"userRole": "",
		"createTime": "",
		"updateTime": ""
	},
	"message": ""
}
```


## userLogout


**接口地址**:`/api/user/logout`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


## userRegister


**接口地址**:`/api/user/register`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "userAccount": "",
  "email": "",
  "userPassword": "",
  "checkPassword": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userRegisterRequest|UserRegisterRequest|body|true|UserRegisterRequest|UserRegisterRequest|
|&emsp;&emsp;userAccount|||false|string||
|&emsp;&emsp;email|||false|string||
|&emsp;&emsp;userPassword|||false|string||
|&emsp;&emsp;checkPassword|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseLong|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||integer(int64)|integer(int64)|
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": 0,
	"message": ""
}
```


## updateUser


**接口地址**:`/api/user/update`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "id": 0,
  "userName": "",
  "userAvatar": "",
  "userProfile": "",
  "userRole": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userUpdateRequest|UserUpdateRequest|body|true|UserUpdateRequest|UserUpdateRequest|
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;userName|||false|string||
|&emsp;&emsp;userAvatar|||false|string||
|&emsp;&emsp;userProfile|||false|string||
|&emsp;&emsp;userRole|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```


## updateMyUser


**接口地址**:`/api/user/update/my`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "userName": "",
  "userAvatar": "",
  "userProfile": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userUpdateMyRequest|UserUpdateMyRequest|body|true|UserUpdateMyRequest|UserUpdateMyRequest|
|&emsp;&emsp;userName|||false|string||
|&emsp;&emsp;userAvatar|||false|string||
|&emsp;&emsp;userProfile|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|BaseResponseBoolean|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||boolean||
|message||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": true,
	"message": ""
}
```