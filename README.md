# gc-boot快速开发

## 一、系统配置说明

```yaml
gc:
#  认证配置
  auth:
#    jwt存储的key
    jwt-key: gc-it
#    是否使用jwt
    jwt: true
#    是否开启URL校验
    url-check: false
#    是否开启开发模式，开发模式不校验
    development: fasle
#    session配置
    session:
#      超时配置（单位：ms）
      timeout:
#        全局超时配置
        global: 1800
#        移动端超时配置
        mobile: 2592000
#        开启记住我超时配置
        remember: 604800
#    忽略的请求路径-list
    ignores:
#      需要忽略的 URL 格式，不考虑请求方法，/public/**默认忽略
      pattern:
      get:
      post:
      delete:
      put:
      head:
      patch:
      options:
      trace:
#  文件系统配置
  file:
#    文件系统类型，可选值：MONGO_DB、LOCAL
    actuatorType: MONGO_DB
#    文件存储的basePath，文件系统类型为LOCAL，改属性必须设置
    basePath:
#  日志系统配置
  log:
#    保存日志的编码列表，默认全部保存
    codes:
#    是否控制台打印
    console: true
#  限流配置
  limit:
#    是否启用全局限流
    global:
#    全局限流单位时间最大访问次数
    global-max:
#    单位时间，默认分钟
    timeout:
#  支付配置
  pay:
#    支付宝支付配置
    ali:
      appId:
#      私钥
      privateKey:
#      公钥
      publicKey:
#      应用公钥证书地址
      appCertPath:
#      支付宝公钥证书地址
      aliPayCertPath:
#      支付宝根证书
      aliPayRootCertPath:
#      支付宝路由地址
      serverUrl:
#      外网访问项目的域名，支付通知中会使用
      domain:
```



## commons工具包

### gc-validate-common

> 校验工具包
>
> 提供校验工具类、spring validator扩展

#### spring validator扩展

##### 1、@Mobile

> 校验手机号

```java
/**
  * 手机
*/
@Mobile(message = "手机号码格式错误")
private String mobile;
```

##### 2、@Contain

> 校验是否包含指定值

属性 | 类型 | 描述
---| ---| ---
allow | String[] | 允许的值 
allowClass | IEnum接口实现枚举 | 允许的枚举类 