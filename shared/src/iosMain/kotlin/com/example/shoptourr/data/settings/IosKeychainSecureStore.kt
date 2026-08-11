package com.example.shoptourr.data.settings

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS Keychain-backed secure store for JWT tokens.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosKeychainSecureStore(
    private val service: String = "com.example.shoptourr.tokens",
) : SecureKeyValueStore {

    override fun getString(key: String): String? = memScoped {
        val query = baseQuery(key)
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        if (status != errSecSuccess) return null
        val data = CFBridgingRelease(result.value) as? NSData ?: return null
        return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    override fun putString(key: String, value: String) {
        remove(key)
        val data = NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        memScoped {
            val query = baseQuery(key)
            CFDictionaryAddValue(query, kSecValueData, CFBridgingRetain(data))
            SecItemAdd(query, null)
        }
    }

    override fun remove(key: String) {
        memScoped {
            val query = baseQuery(key)
            SecItemDelete(query)
        }
    }

    private fun baseQuery(account: String): CFDictionaryRef {
        val dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null)
            ?: error("Unable to create keychain query")
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(dict, kSecAttrService, CFBridgingRetain(NSString.create(string = service)))
        CFDictionaryAddValue(dict, kSecAttrAccount, CFBridgingRetain(NSString.create(string = account)))
        return dict
    }
}
