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
 * Domain object representing a mobility mode_only JSON data packet.
 * 
 * @author selsky
 */
public class MobilityModeOnlyDataPacket extends MetadataDataPacket {
	private String _mode;
	
	public MobilityModeOnlyDataPacket() {
		
	}

	public String getMode() {
		return _mode;
	}

	public void setMode(String mode) {
		_mode = mode;
	}

	@Override
	public String toString() {
		return "MobilityModeOnlyDataPacket [_mode=" + _mode + " " + super.toString() + "]";
	}
	
}
