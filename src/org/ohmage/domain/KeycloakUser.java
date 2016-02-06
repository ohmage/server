/*******************************************************************************
 * Copyright 2016 The Regents of the University of California
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

import org.ohmage.exception.DomainException;
import org.ohmage.domain.User;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.MalformedClaimException;

/**
 * An internal representation of a keycloak user. The username and email should be set
 * and this class will handle the abstraction between the generic internal user and the
 * keycloak user.  Namely, this user will have a static, unused entry in the password field
 * and will have an additional boolean=true column in the db called "external". If we can
 * obtain the necessary bits from the keycloak server to set personal info, we'll do that too.
 * At this time, the username/email address and any personal info are "imported" once. They wont
 * sync with the content from keycloak.  This should be fixed.
 * 
 * @author Steve Nolen
 */
public class KeycloakUser extends User {
  private String email;
  private UserPersonal personalInfo;
  private static final Boolean external = true;
  private static final String KEY_CLAIM_EMAIL = "email";
  private static final String KEY_CLAIM_FIRST_NAME = "given_name";
  private static final String KEY_CLAIM_LAST_NAME = "family_name";
  private static final String KEY_CLAIM_ORGANIZATION = "org";
  private static final String KEY_CLAIM_PERSONAL_ID = "pid";
  public static final String KEYCLOAK_USER_PASSWORD = "external";

  public KeycloakUser(
      final String username,
      final JwtContext context) 
      throws DomainException {
    
    super(username, KEYCLOAK_USER_PASSWORD, true);
    try {
      this.email = context.getJwtClaims().getClaimValue(KEY_CLAIM_EMAIL, String.class);
      String tFirstName = context.getJwtClaims().getClaimValue(KEY_CLAIM_FIRST_NAME, String.class);
      String tLastName = context.getJwtClaims().getClaimValue(KEY_CLAIM_LAST_NAME, String.class);
      String tOrganization = context.getJwtClaims().getClaimValue(KEY_CLAIM_ORGANIZATION, String.class);
      String tPersonalId = context.getJwtClaims().getClaimValue(KEY_CLAIM_PERSONAL_ID, String.class);
      
      if (tFirstName != null 
    		  && tLastName != null
    		  && tOrganization != null
    		  && tPersonalId != null) {
    	  this.personalInfo =
    			  new UserPersonal(
    					  tFirstName, 
    					  tLastName, 
    					  tOrganization, 
    					  tPersonalId);  	
      }
      
    }
    catch (MalformedClaimException e) {
      /** JWT was not able to be parse. This is very bad since 
       *  user should have passed signature and verification
       *  by this point. 
       */
      throw
        new DomainException(
          "There was an issue parsing the JWT to obtain a user",
          e);
    }
  }

  /**
   * Returns the email of this user.
   * 
   * @return The email of this user.
   */
  public String getEmail() {
    return email;
  }
 
  /**
   * Returns whether or not this user is logged in.
   * 
   * @return Whether or not this user is logged in.
   */
  public Boolean isExternal() {
    return external;
  }
  
  /**
   * Returns user personal info.
   * 
   * @return user personal info. may be null.
   */
  public UserPersonal getPersonalInfo(){
    return personalInfo;
  }

  /**
   * Returns a String dump of this user.
   */
  @Override
  public String toString() {
    return "KeycloakUser [username=" + this.getUsername() + ", password=omitted"
        + ", personalinfo=" + this.getPersonalInfo()
        + "]";
  }
}