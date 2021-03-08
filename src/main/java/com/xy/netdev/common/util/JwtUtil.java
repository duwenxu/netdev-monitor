package com.xy.netdev.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.xy.common.exception.BaseException;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @Author Scott
 * @Date 2018-07-12 14:23
 * @Desc JWT工具类
 **/
public class JwtUtil {

	// Token过期时间30分钟（用户登录过期时间是此时间的两倍，以token在reids缓存时间为准）
	public static final long EXPIRE_TIME = 30 * 60 * 1000;

	/**
	 * 校验token是否正确
	 *
	 * @param token  密钥
	 * @param secret 用户的密码
	 * @return 是否正确
	 */
	public static boolean verify(String token, String username, String secret) {
		try {
			// 根据密码生成JWT效验器
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
			// 效验TOKEN
			DecodedJWT jwt = verifier.verify(token);
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	/**
	 * 获得token中的信息无需secret解密也能获得
	 *
	 * @return token中包含的用户名
	 */
	/*public static String getUsername(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);
			return jwt.getClaim("username").asString();
		} catch (JWTDecodeException e) {
			return null;
		}
	}*/

	/**
	 * 获得token中的信息无需secret解密也能获得
	 *
	 * @return token中包含的用户名
	 */
	public static Integer getUserId(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);
			return jwt.getClaim("userId").asInt();
		} catch (JWTDecodeException e) {
			return null;
		}
	}

	/**
	 * 生成签名,5min后过期
	 *
	 * @param userId 用户id
	 * @param secret   用户的密码
	 * @return 加密的token
	 */
	public static String sign(Integer userId, String secret) {
		Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
		Algorithm algorithm = Algorithm.HMAC256(secret);
		// 附带username信息
		return JWT.create().withClaim("username", userId).withExpiresAt(date).sign(algorithm);

	}

	/**
	 * 根据request中的token获取用户id
	 *
	 * @param request
	 * @return
	 * @throws BaseException
	 */
	public static Integer getUserIdByToken(HttpServletRequest request) throws BaseException {
		/*String accessToken = request.getHeader("X-Access-Token");
		Integer userId = getUserId(accessToken);
		if (ConvertUtils.isEmpty(userId)) {
			throw new BaseException("未获取到用户");
		}
		return userId;*/
		return 1;
	}

}
