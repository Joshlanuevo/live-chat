package com.ym.base.constant

/**
 * Http 网络请求响应码
 * 响应分为五类：
 *      信息响应(100–199)，
 *      成功响应(200–299)，
 *      重定向(300–399)，
 *      客户端错误(400–499)
 *      服务器错误 (500–599)
 *
 *      内部定义协议
 *          600 空消息
 *          601 网络请求过程中内部异常
 */
object HttpResponseCode {

    const val SUCCESS = 200


    const val HTTP_LOCAL_EMPTY = 600
    const val HTTP_LOCAL_DEFAULT_ERROR = 601

    val httpCode = mapOf(

        //信息响应
        100 to "Continue",
        101 to "Switching Protocol",
        102 to "Processing (WebDAV)",
        103 to "Early Hints",

        //成功响应
        200 to "OK",
        201 to "Created",
        202 to "Accepted",
        203 to "Non-Authoritative Information",
        203 to "Non-Authoritative Information",
        204 to "No Content",
        205 to "Reset Content",
        206 to "Partial Content",
        207 to "Multi-Status (WebDAV)",
        208 to "Already Reported (WebDAV)",
        226 to "IM Used (HTTP Delta encoding)",

        //重定向
        300 to "Multiple Choice",
        301 to "Moved Permanently",
        302 to "Found",//请求的资源现在临时从不同的 URI 响应请求
        303 to "See Other",
        304 to "Not Modified",
        305 to "Use Proxy",
        306 to "unused",
        307 to "Temporary Redirect",
        308 to "Permanent Redirect",

        //客户端错误
        400 to "Bad Request",  //请求参数有误
        401 to "Unauthorized",  //当前请求需要用户验证。
        402 to "Payment Required",  //保留
        403 to "Forbidden", //服务器已经理解请求，但是拒绝执行它。
        404 to "Not Found",//请求失败，请求所希望得到的资源未被在服务器上发现。
        405 to "Method Not Allowed",//请求行中指定的请求方法不能被用于请求相应的资源
        406 to "Not Acceptable",//请求的资源的内容特性无法满足请求头中的条件，因而无法生成响应实体。
        407 to "Proxy Authentication Required",//与401响应类似，只不过客户端必须在代理服务器上进行身份验证。
        408 to "Request Timeout",//请求超时
        409 to "Conflict",//由于和被请求的资源的当前状态之间存在冲突，请求无法完成。
        410 to "Gone",//被请求的资源在服务器上已经不再可用，而且没有任何已知的转发地址。
        411 to "Length Required",//服务器拒绝在没有定义 Content-Length 头的情况下接受请求。
        412 to "Precondition Failed",//服务器在验证在请求的头字段中给出先决条件时，没能满足其中的一个或多个。
        413 to "Payload Too Large",//服务器拒绝处理当前请求，因为该请求提交的实体数据大小超过了服务器愿意或者能够处理的范围。
        414 to "URI Too Long",//请求的URI 长度超过了服务器能够解释的长度，因此服务器拒绝对该请求提供服务
        415 to "Unsupported Media Type",//对于当前请求的方法和所请求的资源，请求中提交的实体并不是服务器中所支持的格式，因此请求被拒绝。
        416 to "Range Not Satisfiable",//如果请求中包含了 Range 请求头，并且 Range 中指定的任何数据范围都与当前资源的可用范围不重合，同时请求中又没有定义 If-Range 请求头，那么服务器就应当返回416状态码。
        417 to "Expectation Failed",//此响应代码意味着服务器无法满足 Expect 请求标头字段指示的期望值
        418 to "I'm a teapot",//服务器拒绝尝试用 Hyper Text Coffee Pot Control Protocol
        421 to "Misdirected Request",//该请求针对的是无法产生响应的服务器。
        422 to "Unprocessable Entity (WebDAV)",//请求格式良好，但由于语义错误而无法遵循。
        423 to "Locked (WebDAV)",//正在访问的资源被锁定。
        424 to "Failed Dependency (WebDAV)",//由于先前的请求失败，所以此次请求失败。
        425 to "Too Early",//服务器不愿意冒着风险去处理可能重播的请求。
        426 to "Upgrade Required",//服务器拒绝使用当前协议执行请求，但可能在客户机升级到其他协议后愿意这样做。
        428 to "Precondition Required",//原始服务器要求该请求是有条件的。
        429 to "Too Many Requests",//用户在给定的时间内发送了太多请求（“限制请求速率”）
        431 to "Request Header Fields Too Large",//服务器不愿意处理请求，因为它的 请求头字段太大（ Request Header Fields Too Large）
        451 to "Unavailable For Legal Reasons",//用户请求非法资源，例如：由政府审查的网页

        //服务器错误
        500 to "Internal Server Error", //服务器遇到了不知道如何处理的情况。
        501 to "Not Implemented", //此请求方法不被服务器支持且无法被处理
        502 to "Bad Gateway", //此错误响应表明服务器作为网关需要得到一个处理这个请求的响应，但是得到一个错误的响应。
        503 to "Service Unavailable", //
        504 to "Gateway Timeout", //当服务器作为网关，不能及时得到响应时返回此错误代码。
        505 to "HTTP Version Not Supported", //服务器不支持请求中所使用的HTTP协议版本
        506 to "Variant Also Negotiates", //服务器没有准备好处理请求。
        507 to "Insufficient Storage", //服务器有内部配置错误
        508 to "Loop Detected (WebDAV)", //服务器在处理请求时检测到无限循环。
        510 to "Not Extended", //客户端需要对请求进一步扩展，服务器才能实现它。
        511 to "Network Authentication Required", //状态码指示客户端需要进行身份验证才能获得网络访问权限。

    )

}