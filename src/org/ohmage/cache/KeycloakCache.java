package org.ohmage.cache;

import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jose4j.base64url.Base64;

/**
 * Singleton cache for the indices and String values for keycloak
 * 
 * @author Steve Nolen
 */
public class KeycloakCache {
	// set the expected returns from the keycloak.json file
	// currently only realmPublicKey is used, as ohmage expects to be
	// bearer only.
	private static String realm = null;
	private static PublicKey realmPublicKey = null;
	private static String authServerUrl = null; 
	private static String sslRequired = null;
	private static String resource = null;
	private static Boolean bearerOnly = null;
	private static Boolean valid = false;

	/**
	 * The public key algorithm to use. keycloak currently supports RSA
	 */
	private static final String PUBLIC_KEY_ALGORITHM = "RSA";
	
	/**
	 * append to a keycloak auth server url to get to correct realm endpoint.
	 */
	private static final String KEYCLOAK_REALM_ENDPOINT = "realms/";
	
	private static final String KEY_KEYCLOAK_REALM = "realm";
	private static final String KEY_KEYCLOAK_REALM_PUBLIC_KEY = "realm-public-key";
	private static final String KEY_KEYCLOAK_AUTH_SERVER_URL = "auth-server-url";
	private static final String KEY_KEYCLOAK_SSL_REQUIRED = "ssl-required";
	private static final String KEY_KEYCLOAK_RESOURCE = "resource";
	private static final String KEY_KEYCLOAK_BEARER_ONLY = "bearer-only";
	private static final String KEY_KEYCLOAK_PUBLIC_KEY_FROM_SERVER = "public_key";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = 
			Logger.getLogger(KeycloakCache.class);

	public static void setCache(
			final Map<String,String> map)
					throws IllegalArgumentException {

		// Null check.
		if(map == null) {
			throw new IllegalArgumentException("The map is null.");
		}

		realm = map.get(KEY_KEYCLOAK_REALM);
		realmPublicKey = getKey(map.get(KEY_KEYCLOAK_REALM_PUBLIC_KEY));
		authServerUrl = map.get(KEY_KEYCLOAK_AUTH_SERVER_URL);
		sslRequired = map.get(KEY_KEYCLOAK_SSL_REQUIRED);
		resource = map.get(KEY_KEYCLOAK_RESOURCE);
		bearerOnly = Boolean.valueOf(map.get(KEY_KEYCLOAK_BEARER_ONLY));
		
		/* send an http request to the keycloak server configured above.
		 * this ensures we have an accurate and usable realm/public key
		 * for authenticating with keycloak prior to acting as a keycloak
		 * bearer.
		 */
		valid = validKeycloakServer(map.get(KEY_KEYCLOAK_REALM_PUBLIC_KEY));
	}

	public static String getRealm() {
		return realm;
	}

	public static PublicKey getPublicKey() {
		return realmPublicKey;
	}
	
	public static Boolean getValid() {
		return valid;
	}
	
	/**
	 * Attempts to determine if keycloak is setup properly.
	 * 
	 * Note that method intentionally does not throw errors as
	 * the server will operate as normal, just without keycloak support.
	 * 
	 * @param String representation of existing cached keycloak public key.
	 * 
	 * @return A Boolean of true if setup is valid, false otherwise.
	 */
	private static Boolean validKeycloakServer(String cachedKey){
		// Null check.
		if(cachedKey == null) {
			throw new IllegalArgumentException("The cachedKey is null.");
		}
		
		String url = getRealmEndpoint();
		
		try {
		    URL myURL = new URL(url);
		    HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();
		    
			// If a non-200 response was returned, get the text from the 
			// response.
			if(urlConnection.getResponseCode() != 200) {
				// Get the error text.
				ByteArrayOutputStream errorByteStream = new ByteArrayOutputStream();
				InputStream errorStream = urlConnection.getErrorStream();
				byte[] chunk = new byte[4096];
				int amountRead;
				while((amountRead = errorStream.read(chunk)) != -1) {
					errorByteStream.write(chunk, 0, amountRead);
				}
				errorStream.close();
				LOGGER.warn("Error from keycloak server: "
						+ urlConnection.getResponseCode()
						+ ", with message: "
						+ errorByteStream.toString());
				return false;
			}
			else {
				// Build the response.
				InputStream reader = urlConnection.getInputStream();

				// Generate the byte array.
				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				byte[] chunk = new byte[4096];
				int amountRead = 0;
				while((amountRead = reader.read(chunk)) != -1) {
					byteArrayStream.write(chunk, 0, amountRead);
				}
				reader.close();
				
				Map<String,String> realmMap;
				try {
					ObjectMapper mapper = new ObjectMapper();
					TypeReference<Map<String, String>> ref = new TypeReference<Map<String, String>>() { };
					realmMap = mapper.readValue(byteArrayStream.toString(), ref);
				}
				catch (JsonParseException e){
					LOGGER.warn("Error while parsing json response from keycloak server.");
					return false;
				}
				
				if (cachedKey.equals(realmMap.get(KEY_KEYCLOAK_PUBLIC_KEY_FROM_SERVER))) {
					// server was successfully contacted,
					// public key matches our cached copy.
					// all is well.
					return true;
				} else {
					LOGGER.warn("Keycloak server broadcasting a public key "
							+ "which does not match our stored key. Stored Key: "
							+ cachedKey
							+ ", Keycloak Key: "
							+ realmMap.get(KEY_KEYCLOAK_PUBLIC_KEY_FROM_SERVER));
					return false;
				}
			}
		} 
		catch (MalformedURLException e) {
			LOGGER.warn("Built a Malformed URL:"
					+ url);
			return false;
		} 
		catch (IOException e) {   
			LOGGER.warn("Error communicating with keycloak server at:"
					+ url);
			return false;
		}
	}
	
	private static String getRealmEndpoint(){
		// Build the request URL.
		StringBuilder urlBuilder = new StringBuilder();
		String serverUrl = authServerUrl;
		urlBuilder.append(serverUrl);

		if(! serverUrl.endsWith("/")) {
			urlBuilder.append("/");
		}
		urlBuilder.append(KEYCLOAK_REALM_ENDPOINT);
		urlBuilder.append(realm);
		return urlBuilder.toString();
	}

	private static PublicKey getKey(String key){
		// Null check.
		if(key == null) {
			throw new IllegalArgumentException("The key is null.");
		}
		try{
			// assume key is not formatted correctly. the key from keycloak is not.
			String pubKey = key.replaceAll("(.{64})", "$1\n");
			byte[] byteKey = Base64.decode(pubKey);
			X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
			KeyFactory kf = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM);
			return kf.generatePublic(X509publicKey);
		}
		catch(InvalidKeySpecException e){
			LOGGER.warn("Imported public key from keycloak.json is invalid");
		}
		catch(NoSuchAlgorithmException e){
			LOGGER.warn("Error importing public key from keycloak.json, no such algorithm: "
					+ PUBLIC_KEY_ALGORITHM);
		}

		return null;
	}

}