/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.domain;

/**
 * Data transfer object for image queries used for filtering out private images.
 * 
 * @author Joshua Selsky
 */
public class UrlPrivacyState {
	
	private String _privacyState;
	private String _url;
	
	public UrlPrivacyState(String url, String privacyState) {
		_url = url;
		_privacyState = privacyState;
	}
	
	public String getPrivacyState() {
		return _privacyState;
	}
	
	public String getUrl() {
		return _url;
	}
}
