@file:JvmName("ImageUrlHelper")

package com.venus.framework.util

import kotlin.properties.Delegates

/**
 * Cloudinary图片transformation pattern
 *
 * /image/<type>/<transformation>.../<version>/<folder>/<public_id>.<format>
 *
 * [Cloudinary文档](http://cloudinary.com/documentation/image_transformation_reference)
 *
 * 目前能够处理的情况请参考单元测试用例
 */
var cloudImageUrlPattern: String by Delegates.observable(
    initialValue = """(.*/image/(?:[\w]+/)?)(?:.*?[_:,].*?/)*?((?:v\d+/)?(?:[-\w]+?/)?[-\w]+\.\w+)$"""
) { _, _, newValue ->
    if (newValue.isNotBlank()) imageUrlRegex = newValue.toRegex()
}
private var imageUrlRegex: Regex = cloudImageUrlPattern.toRegex()

/** 处理Cloudinary图片url */
fun transformCloudImageUrl(originUrl: String, params: String = ""): String =
    imageUrlRegex.replace(originUrl, "$1$params/$2")
