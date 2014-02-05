/**
 * Licensed to ohmage under one or more contributor license agreements. See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ohmage team licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * with this source code or at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/**
 * <p>A Concordia object has one constructor which takes a schema and validates
 * it. If it fails validation, an exception will be thrown.</p>
 * 
 * <p>There are two public functions, conformsTo(schema) and 
 * validateData(data).</p>
 * 
 * @author John Jenkins
 */
function Concordia(schema) {
    'use strict';

    // An anonymous function is used to create the internals of the class and
    // prevent those functions and data from being exposed.
    (function (concordia) {
        // The Concordia keywords.
        var KEYWORD_TYPE = "type"
          , KEYWORD_OPTIONAL = "optional"
          , KEYWORD_DOC = "doc"
          , KEYWORD_SCHEMA = "schema"
          , KEYWORD_FIELDS = "fields"
          , KEYWORD_CONST_TYPE = "constType"
          , KEYWORD_CONST_LENGTH = "constLength"
          , KEYWORD_NAME = "name"
          , KEYWORD_REFERENCE = "$ref"
          , KEYWORD_CONCORDIA = "$concordia"

        // The Concordia types.
          , TYPE_BOOLEAN = "boolean"
          , TYPE_NUMBER = "number"
          , TYPE_STRING = "string"
          , TYPE_OBJECT = "object"
          , TYPE_ARRAY = "array"

        // The JavaScript types.
          , JS_TYPE_BOOLEAN = "[object Boolean]"
          , JS_TYPE_NUMBER = "[object Number]"
          , JS_TYPE_STRING = "[object String]"
          , JS_TYPE_OBJECT = "[object Object]"
          , JS_TYPE_ARRAY = "[object Array]"
          , JS_TYPE_FUNCTION = "[object Function]";
        
        /**
         * Apparently, indexOf is a little non-standard, so we offer an
         * implementation if it doesn't already exist.
         */
        if (! Array.prototype.indexOf) {
            Array.prototype.indexOf =
                function(value) {
                    for(var i = 0; i < this.length; i++) {
                        if(this[i] === value) {
                            return i;
                        }
                    }
                    return -1;
                };
        }

        // Predefine the recursive functions to allow them to be referenced
        // before they are defined.
        function validateSchemaInternal(obj) {}
        function validateDataInternal(schema, data) {}
        
        /**
         * Retrieves a remote schema via the {@link #KEYWORD_REFERENCE} field
         * and returns it.
         * 
         * @param obj The object with a {@link #KEYWORD_REFERENCE} field that
         *            reference the remote schema to be downloaded, compiled,
         *            and returned.
         *            
         * @return A compiled Concordia object if a {@link #KEYWORD_REFERENCE}
         *         field existed; otherwise, null is returned. 
         */
        function getRemoteSchema(obj) {
            // Attempt to get the reference string from the object.
            var ref = obj[KEYWORD_REFERENCE];
            // If the value is JSON null, throw an exception indicating that.
            if (ref === null) {
                throw "The '" +
                        KEYWORD_REFERENCE +
                        "' field for the JSON object is null, which is not " +
                        "allowed: " +
                        JSON.stringify(obj);
            }
            var refType = typeof ref;
            // If the reference field is missing, return.
            if (refType === "undefined") {
                return null;
            }
            // If the reference field does exist but it isn't a string, that is
            // an error.
            if (Object.prototype.toString.call(ref) !== JS_TYPE_STRING) {
                throw "The '" +
                        KEYWORD_REFERENCE +
                        "' field for the JSON object is not a string, which " +
                        "it must be to reference an external schema: " +
                        JSON.stringify(obj);
            }
            
            // Get the referenced schema.
            var subSchemaRequest = new XMLHttpRequest();
            subSchemaRequest.open("GET", ref, false);
            subSchemaRequest.send(null);
            
            // Verify that the request succeeded.
            if(subSchemaRequest.status !== 200) {
                throw "The sub-schema could not be retrieved (" +
                        subSchemaRequest.status +
                        "): " +
                        subSchemaRequest.responseText;
            }
            
            // Get the response text.
            var subSchemaString = subSchemaRequest.responseText;
            if ((subSchemaString === null) || 
                (subSchemaString === "")) {
                
                throw "The sub-schema was not returned from the remote " +
                        "location: " +
                        ref;
            }
            
            // Create a Concordia object from this remote Concordia schema and
            // return it.
            return new Concordia(subSchemaString);
        }
        
        /**
         * <p>Retrieves a remote schema definition if the corresponding key
         * exists, validates that the remote schema is valid by creating a
         * Concordia object from it, and stores that Concordia object with the
         * original reference object.</p>
         * 
         * <p>The key to use to reference a remote schema is
         * {@link #KEYWORD_REFERENCE} and the value of that key must be a URL
         * string that can be used to retrieve the schema. The decomposed
         * object is then stored back in the original object under the key
         * {@link #KEYWORD_CONCORDIA}. Note that this means that anything that
         * was previously stored under this key will be overridden, so it is
         * advised to never use that key.</p>
         * 
         * @param obj The object that may be a remote reference to a schema.
         */
        function storeRemoteSchema(obj) {
            // Get the remote schema.
            var subSchema = getRemoteSchema(obj);
            
            // If there was no reference to a remote schema, null is returned.
            if(subSchema === null) {
                return;
            }
            
            // Update its data validation function to the internal one.
            subSchema.validateData = validateDataNoCheck;
            
            // Store the sub-schema in this object alongside the reference.
            obj[KEYWORD_CONCORDIA] = subSchema;
        }
        
        /**
         * Validates a JSON object whose "type" has already been determined to
         * be "boolean". There are no other requirements for the "boolean" 
         * type.
         * 
         * @param obj The JSON object to validate.
         */
        function validateSchemaBoolean(obj) {
            // Check if any additional properties were added to this type.
            var decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateSchemaDecoratorBoolean);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateSchemaDecoratorBoolean(obj);
            }
        }
        
        /**
         * Validates that a schema that is extending another is presently
         * defining a boolean value.
         * 
         * @param original The part of the original schema that is defining a
         *                 boolean.
         * 
         * @param extender The part of the schema that is extending the
         *                 original schema. This must define a boolean.
         */
        function validateSchemaExtenderBoolean(original, extender) {
            if (extender[KEYWORD_TYPE] !== TYPE_BOOLEAN) {
                throw "The original schema defined a boolean, but the " +
                        "extending schema does not: " +
                        JSON.stringify(extender);
            }
        }
        
        /**
         * Validates that the data is a boolean or, if it is 'null' or missing,
         * that the field is optional.
         * 
         * @param schema The Schema to use to validate the data.
         * 
         * @param data The data to validate.
         */
        function validateDataBoolean(schema, data) {
            // If the data is not present or 'null', ensure that it is 
            // optional.
            if ((typeof data === "undefined") || (data === null)) {
                if (! schema[KEYWORD_OPTIONAL]) {
                    throw "The data is null and not optional.";
                }
            }
            // If the data is present, ensure that it is a boolean.
            else if (Object.prototype.toString.call(data) !== JS_TYPE_BOOLEAN) {
                throw "The value is not a boolean: " + JSON.stringify(data);
            }
            
            // Check if custom validation code is present.
            var decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateDataDecoratorBoolean);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateDataDecoratorBoolean(schema, data);
            }
        }
        
        /**
         * Validates a JSON object whose "type" has already been determined to
         * be "number". There are no other requirements for the "number" type.
         * 
         * @param obj The JSON object to validate.
         */
        function validateSchemaNumber(obj) {
            // Check if any additional properties were added to this type.
            var decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateSchemaDecoratorNumber);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateSchemaDecoratorNumber(obj);
            }
        }
        
        /**
         * Validates that a schema that is extending another is presently
         * defining a number value.
         * 
         * @param original The part of the original schema that is defining a
         *                 number.
         * 
         * @param extender The part of the schema that is extending the
         *                 original schema. This must define a number.
         */
        function validateSchemaExtenderNumber(original, extender) {
            if (extender[KEYWORD_TYPE] !== TYPE_NUMBER) {
                throw "The original schema defined a number, but the " +
                		"extending schema does not: " +
                		JSON.stringify(extender);
            }
        }
        
        /**
         * Validates that the data is a number or, if it is 'null' or missing,
         * that the field is optional. 
         * 
         * @param schema The schema to use to validate the data.
         * 
         * @param data The data to validate.
         */
        function validateDataNumber(schema, data) {
            // If the data is not present or 'null', ensure that it is
            // optional.
            if ((typeof data === "undefined") || (data === null)) {
                if (! schema[KEYWORD_OPTIONAL]) {
                    throw "The data is null and not optional.";
                }
            }
            // If the data is present, ensure that it is a number.
            else if (Object.prototype.toString.call(data) !== JS_TYPE_NUMBER) {
                throw "The value is not a number: " + JSON.stringify(data);
            }
            
            // Check if custom validation code is present.
            var decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateDataDecoratorNumber);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateDataDecoratorNumber(schema, data);
            }
        }
        
        /**
         * Validates a JSON object whose "type" has already been determined to
         * be "string". There are no other requirements for the "string" type.
         * 
         * @param obj The JSON object to validate.
         */
        function validateSchemaString(obj) {
            // Check if any additional properties were added to this type.
            var decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateSchemaDecoratorString);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateSchemaDecoratorString(obj);
            }
        }
        
        /**
         * Validates that a schema that is extending another is presently
         * defining a string value.
         * 
         * @param original The part of the original schema that is defining a
         *                 string.
         * 
         * @param extender The part of the schema that is extending the
         *                 original schema. This must define a string.
         */
        function validateSchemaExtenderString(original, extender) {
            if (extender[KEYWORD_TYPE] !== TYPE_STRING) {
                throw "The original schema defined a string, but the " +
                        "extending schema does not: " +
                        JSON.stringify(extender);
            }
        }
        
        /**
         * Validates that the data is a string or, if it is 'null' or missing,
         * that the field is optional.
         */
        function validateDataString(schema, data) {
            // If the data is not present or 'null', ensure that it is 
            // optional.
            if ((typeof data === "undefined") || (data === null)) {
                if (! schema[KEYWORD_OPTIONAL]) {
                    throw "The data is null and not optional.";
                }
            }
            // If the data is present, ensure that it is a string.
            else if (Object.prototype.toString.call(data) !== JS_TYPE_STRING) {
                throw "The data is not a string: " + JSON.stringify(data);
            }
            
            // Check if custom validation code is present.
            var decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateDataDecoratorString);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateDataDecoratorString(schema, data);
            }
        }
        
        /**
         * Validates a JSON object whose "type" has already been determined to
         * be "object". The "object" type requires a "schema" field whose value
         * is a JSON array of JSON objects. Each JSON object defines a field in
         * the data that conforms to this schema; therefore, each JSON object
         * must have a "name" field whose value will be the key in the data.
         * Each JSON object must also define a type.
         * 
         * @param obj The JSON object to validate.
         */
        function validateSchemaObject(obj) {
            var fields = obj[KEYWORD_FIELDS]
              , fieldsType
              , i
              , j
              , field
              , fieldNames
              , name
              , nameType
              , decoratorType;
            
            // Verify the schema isn't null.
            if (fields === null) {
                throw "The '" + 
                        KEYWORD_FIELDS + 
                        "' field's value is null: " + 
                        JSON.stringify(obj);
            }
            // Verify that the schema is present and is a JSON array.
            fieldsType = typeof fields;
            if (fieldsType === "undefined") {
                throw "The '" + 
                        KEYWORD_FIELDS + 
                        "' field is missing: " + 
                        JSON.stringify(obj);
            }
            if (Object.prototype.toString.call(fields) !== JS_TYPE_ARRAY) {
                throw "The '" +
                        KEYWORD_FIELDS +
                        "' field's value must be a JSON array: " + 
                        JSON.stringify(obj);
            }
            
            // The list of field names needs to be initialized.
            fieldNames = [];
            
            // For each of the JSON objects, verify that it has a name and a
            // type.
            for (i = 0; i < fields.length; i += 1) {
                // Get the field.
                field = fields[i];
                
                // Verify that the field isn't null.
                if (field === null) {
                    throw "The element at index " + 
                            i + 
                            " of the '" +
                            KEYWORD_FIELDS +
                            "' field is null: " + 
                            JSON.stringify(obj);
                }
                // Verify that the index is a JSON object and not an array.
                if (Object.prototype.toString.call(field) !== JS_TYPE_OBJECT) {
                    throw "The element at index " + 
                            i + 
                            " of the '" +
                            KEYWORD_FIELDS +
                            "' field is not a JSON object: " + 
                            JSON.stringify(obj);
                }
                
                // Validates the type of this field.
                validateSchemaInternal(field);

                // Verify that the JSON object contains a "name" field and that
                // it's not null.
                name = field[KEYWORD_NAME];
                if (name === null) {
                    throw "The '" +
                            KEYWORD_NAME +
                            "' field for the JSON object at index " + 
                            i + 
                            " is null: " + 
                            JSON.stringify(obj);
                }
                // Verify that the "name" or "$ref" fields exist. 
                nameType = typeof name;
                if (nameType === "undefined") {
                    // If a remote schema was not added, then throw an
                    // exception regarding the missing "name" field.
                    if ((field[KEYWORD_CONCORDIA] === null) ||
                        (typeof field[KEYWORD_CONCORDIA] === "undefined")) {
                        
                        throw "The '" +
                                KEYWORD_NAME +
                                "' field for the JSON object at index " + 
                                i + 
                                " is misisng: " + 
                                JSON.stringify(obj);
                    }
                    
                    // Get the remote schema.
                    var remoteSchema =
                        field[KEYWORD_CONCORDIA][KEYWORD_SCHEMA];
                    
                    // Check the type of the remote schema and ensure that it
                    // is an object.
                    if (remoteSchema[KEYWORD_TYPE] !== TYPE_OBJECT) {
                        throw "The root type of the sub-schema must be a '" +
                                TYPE_OBJECT +
                                "' in order to extend an object's fields: " +
                                JSON.stringify(concordia);
                    }
                    
                    // Check the fields in the remote object and ensure that
                    // they do not overlap with these fields.
                    var otherFields =
                        getSchemaObjectFields(remoteSchema[KEYWORD_FIELDS]);
                    for (j = 0; j < otherFields.length; j++) {
                        // Get the current field name.
                        var currName = otherFields[j];
                        
                        // Verifies that no field with that name already
                        // exists.
                        if (fieldNames.indexOf(currName) !== -1) {
                            throw "The field '" +
                                    currName +
                                    "' is defined multiple times: " +
                                    JSON.stringify(obj);
                        }
                        // Add this field to the list of fields.
                        else {
                            fieldNames.push(currName);
                        }
                    }
                }
                // If the "name" field does exist, it must be a string.
                else if (Object.prototype.toString.call(name) !== JS_TYPE_STRING) {
                    throw "The type of the '" +
                            KEYWORD_NAME +
                            "' field for the JSON object at index " + 
                            i + 
                            " is not a string: " + 
                            JSON.stringify(obj);
                }
                // Validate the field.
                else {
                    // Verifies that no field with that name already exists.
                    if (fieldNames.indexOf(name) !== -1) {
                        throw "The field '" +
                                name +
                                "' is defined multiple times: " +
                                JSON.stringify(obj);
                    }
                    // Add this field to the list of fields.
                    else {
                        fieldNames.push(name);
                    }
                }
            }
            
            // Check if any additional properties were added to this type.
            decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateSchemaDecoratorObject);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateSchemaDecoratorObject(obj);
            }
        }
        
        /**
         * Retrieves all of the field names from a list of fields.
         * 
         * @param fields The fields from which to retrieve the field names.
         * 
         * @return An array of field names.
         */
        function getSchemaObjectFields(fields) {
            var i
              , field
              , fieldName
              , result = [];
            
            // Cycle through the fields adding their name or sub-schema names.
            for (i = 0; i < fields.length; i++) {
                // Get the field.
                field = fields[i];
                // Get the field's name.
                fieldName = field[KEYWORD_NAME];

                // If this field doesn't have a name, then it must have a
                // sub-object, so we must cycle through that.
                if (typeof fieldName === "undefined") {
                    // Add all of the sub-schema's names.
                    result
                        .concat(
                            getSchemaObjectFields(
                                field[KEYWORD_CONCORDIA][KEYWORD_SCHEMA]));
                }
                // Otherwise, just add this field name.
                else {
                    result.push(fieldName);
                }
            }
            
            return result;
        }
        
        /**
         * Verifies that the extending schema defines, at least, all of the
         * fields defined in the original schema and, for each field, recurses
         * on their type to ensure that it matches as well.
         * 
         * @param original The original schema that is being extended.
         * 
         * @param extender The schema that is extending the original schema.
         */
        function validateSchemaExtenderObject(original, extender) {
            if (extender[KEYWORD_TYPE] !== TYPE_OBJECT) {
                throw "The original schema defined an object, but the " +
                        "extending schema does not: " +
                        JSON.stringify(extender);
            }
            
            // Get the extender's fields.
            var extenderFields = extender[KEYWORD_FIELDS];
            
            // Get the original fields and cycle through them.
            var originalFields = original[KEYWORD_FIELDS];
            for (var i = 0; i < originalFields.length; i += 1) {
                // Get the field at this index.
                var originalField = originalFields[i];
                
                // Get the name.
                var originalName = originalField[KEYWORD_NAME];
                
                // If the original didn't have a name then it must have a
                // sub-schema with names.
                if (typeof originalName === "undefined") {
                    // Recurse on the sub-object.
                    validateSchemaExtender(
                        originalField[KEYWORD_CONCORDIA],
                        extender);
                }
                else {
                    // Get the definition from the extender.
                    var extenderField =
                        getSchemaObjectField(extenderFields, originalName);
                    
                    // If the field doesn't exist, check if it is optional.
                    if (extenderField === null) {
                        // Get the "optional" field.
                        var optional = originalField[KEYWORD_OPTIONAL];
                        
                        // The only way to succeed at this point is if the
                        // original defined it as optional.
                        if ((typeof optional === "undefined") ||
                            (! optional)) {
                        
                            throw "The original schema has a field that is " +
                            		"not optional and not found in the " +
                            		"extending schema ('" +
                            		originalName +
                            		"'): " +
                            		JSON.stringify(extender);
                        }
                    }
                    // Recurse on the field.
                    else {
                        // Recurse on the original field and this field.
                        validateSchemaExtender(originalField, extenderField);
                    }
                }
            }
        }
        
        /**
         * For an array of fields defining an object, return the schema for the
         * field whose name matches the given name.
         * 
         * @param fields The array of objects defining the fields.
         * 
         * @param name The field name whose name should be returned.
         * 
         * @return The object defining the field or null if no such field
         *         exists.
         */
        function getSchemaObjectField(fields, name) {
            var i
              , field
              , fieldName;
            
            // Cycle through the fields.
            for (i = 0; i < fields.length; i++) {
                // Get the field.
                field = fields[i];
                // Get the field's name.
                fieldName = field[KEYWORD_NAME];
                
                // If this field doesn't have a name, then it must have a
                // sub-object, so we must cycle through that.
                if (typeof fieldName === "undefined") {
                    // Recurse on the sub-object.
                    var result =
                        getSchemaObjectField(
                            // Get the sub-object's schema's list of fields.
                            field[KEYWORD_CONCORDIA][KEYWORD_SCHEMA][KEYWORD_FIELDS],
                            name);
                    
                    // If an applicable field was found, return it.
                    if (result !== null) {
                        return result;
                    }
                }
                // If this field does have a name and it matches the name we
                // wanted, return this field.
                else if (fieldName === name) {
                    return field;
                }
            }
            
            // If no field was ever found, return null.
            return null;
        }
        
        /**
         * Validates that the data is a JSON object or, if the data is 'null'
         * or missing, that it is optional.
         * 
         * @param schema The schema to use to validate the data.
         * 
         * @param data The data to validate.
         */
        function validateDataObject(schema, data) {
            var i
              , schemaFields
              , schemaField
              , name
              , dataField
              , decoratorType;
            
            // If the data is not present or 'null', ensure that it is 
            // optional.
            if ((typeof data === "undefined") || (data === null)) { 
                if (! schema[KEYWORD_OPTIONAL]) {
                    throw "The object data is not optional: " +
                            JSON.stringify(schema);
                }
                else {
                    return;
                }
            }
            
            // Ensure that it is an object.
            if (Object.prototype.toString.call(data) !== JS_TYPE_OBJECT) {
                throw "The data is not a JSON object: " + JSON.stringify(data);
            }
            
            // For each index in the object's "schema" field,
            schemaFields = schema[KEYWORD_FIELDS];
            for (i = 0; i < schemaFields.length; i += 1) {
                // Get this index, which is a JSON object that contains the
                // type schema.
                schemaField = schemaFields[i];
                
                // Get the name.
                name = schemaField[KEYWORD_NAME];
                
                // Get the data that corresponds to that field.
                dataField = data[name];
                
                // Validate the data.
                validateDataInternal(schemaField, dataField);
            }
            
            // Check if custom validation code is present.
            decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateDataDecoratorObject);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateDataDecoratorObject(schema, data);
            }
        }
        
        /**
         * Validates a JSON object whose "type" has already been determined to
         * be "array". The "array" type requires either a
         * {@link #KEYWORD_CONST_TYPE} or a {@link #KEYWORD_CONST_LENGTH} field
         * that defines a sub-schema for that type of array.
         * 
         * @param obj The JSON object to validate.
         * 
         * @see validateConstLengthArray(object)
         * @see validateConstTypeArray(object)
         */
        function validateSchemaArray(obj) {
            var constTypeType = typeof obj[KEYWORD_CONST_TYPE]
              , constLengthType = typeof obj[KEYWORD_CONST_LENGTH]
              , decoratorType;
            
            // The array must either be a constant-length array or a
            // constant-type array, not both.
            if ((constTypeType !== "undefined") &&
                (constLengthType !== "undefined")) {
                
                throw "An array's definition defined both a constant-length " +
                		"and a constant-type sub-schema. Only one may be " +
                		"defined: " +
                		JSON.stringify(obj);
            }
            // If it is a constant-type array, validate that.
            else if (constTypeType !== "undefined") {
                validateSchemaConstTypeArray(obj);
            }
            // If it is a constant-length array, validate that.
            else if (constLengthType !== "undefined") {
                validateSchemaConstLengthArray(obj);
            }
            // Otherwise, neither definition was given and an exception should
            // be thrown.
            else {
                throw "An array's definition did not define a constant-type " +
                		"or a constant-length sub-schema: " +
                		JSON.stringify(obj);
            }
            
            // Check if any additional properties were added to this type.
            decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateSchemaDecoratorArray);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateSchemaDecoratorArray(obj);
            }
        }
        
        /**
         * Validates that an extending schema defines has the same schema as
         * original schema. For constant-type arrays, this means that the
         * singular object is the same. For constant-length arrays, the schemas
         * must be equal on an index-by-index basis.
         * 
         * @param original The original schema that is being extended.
         * 
         * @param extender The schema that is extending the original schema.
         */
        function validateSchemaExtenderArray(original, extender) {
            // Ensure that the extending schema is 
            if (extender[KEYWORD_TYPE] !== TYPE_ARRAY) {
                throw "The original schema defined an array, but the " +
                        "extending schema does not: " +
                        extender;
            }
            
            // Get the different types for an array.
            var originalConstType = original[KEYWORD_CONST_TYPE];
            var originalConstLength = original[KEYWORD_CONST_LENGTH];
            
            // If it is a constant-type array, ensure that the extender is also
            // defining a constant-type array.
            if (typeof originalConstType !== "undefined") {
                // Get the type of array for the extender schema.
                var extenderConstType = extender[KEYWORD_CONST_TYPE];
                
                // Ensure that the extender schema is a constant-type as well.
                if (typeof extenderConstType === "undefined") {
                    throw "The original schema defined a constant-type " +
                            "array, but the extending schema did not: " +
                            JSON.stringify(extender);
                }
                
                // Validate the sub-schemas.
                validateSchemaExtender(originalConstType, extenderConstType);
            }
            // Otherwise, it must be a constant-length array, so ensure that
            // the extender is also defining a constant-lenght array.
            else {
                // Get the type of array for the extender schema.
                var extenderConstLength = extender[KEYWORD_CONST_LENGTH];

                // Ensure that the extender schema is a constant-length as well.
                if (typeof extenderConstLength === "undefined") {
                    throw "The original schema defined a constant-type " +
                            "array, but the extending schema did not: " +
                            JSON.stringify(extender);
                }

                // Ensure that the two schemas are the same length. 
                if (originalConstLength.length !== extenderConstLength.length) {
                    throw "The original schema and the extending schema " +
                            "are different lengths: " +
                            JSON.stringify(extender);
                }
                
                // Ensure each index defines the same schema.
                for (var i = 0; i < originalConstLength.length; i++) {
                    validateSchemaExtender(
                        originalConstLength[i],
                        extenderConstLength[i]);
                }
            }
        }
        
        /**
         * Validates that the data is a JSON array or, if the data is missing
         * or 'null', that it is optional.
         * 
         * @param schema The schema to use to validate the data.
         * 
         * @param data The data to validate.
         * 
         * @see validateConstLengthArray(object, array)
         * @see validateConstTypeArray(object, array)
         */
        function validateDataArray(schema, data) {
            var decoratorType;
            
            // If the data is not present or 'null', ensure that it is
            // optional.
            if ((typeof data === "undefined") || (data === null)) { 
                if (! schema[KEYWORD_OPTIONAL]) {
                    throw "The array data is not optional: " +
                            JSON.stringify(schema);
                }
                else {
                    return;
                }
            }
            
            // Ensure it is an array.
            if (Object.prototype.toString.call(data) !== JS_TYPE_ARRAY) {
                throw "The data is not a JSON array: " + 
                        JSON.stringify(data);
            }
            
            // If it's a constant-type array, validate that.
            if (typeof schema[KEYWORD_CONST_TYPE] !== "undefined") {
                validateDataConstTypeArray(schema, data);
            }
            // If it's a constant-length array, validate that.
            else {
                validateDataConstLengthArray(
                    schema,
                    data);
            }
            
            // Check if custom validation code is present.
            decoratorType = 
                Object
                    .prototype
                        .toString
                            .call(
                                Concordia
                                    .prototype.validateDataDecoratorArray);
            if (decoratorType === JS_TYPE_FUNCTION) {
                Concordia.prototype.validateDataDecoratorArray(schema, data);
            }
        }
        
        /**
         * Validates a JSON object that is defining the type for all of the 
         * indicies in a JSON array.
         * 
         * @param obj The JSON object to validate.
         */
        function validateSchemaConstTypeArray(obj) {
            var constType = obj[KEYWORD_CONST_TYPE]
              , constTypeType;
          
            // Ensure that the sub-schema exists.
            if (constType === null) {
                throw "The sub-schema for a constant type array is null: " +
                        JSON.stringify(obj);
            }
            // Ensure that the sub-schema is an object.
            constTypeType = Object.prototype.toString.call(constType);
            if (constTypeType !== JS_TYPE_OBJECT) {
                throw "The sub-schema for a constant type array is not of " +
                        "type '" +
                        JS_TYPE_OBJECT +
                        "': "
                        + JSON.stringify(obj);
            }

            // Validate the array's type.
            validateSchemaInternal(constType);
        }
        
        /**
         * Validates that each element in an array has the given schema.
         * 
         * @param schema The Concordia schema to use to validate the data.
         * 
         * @param dataArray The array of elements to validate.
         */
        function validateDataConstTypeArray(schema, dataArray) {
            var i;
            
            // For each element in the data array, make sure that it conforms
            // to the given schema.
            for (i = 0; i < dataArray.length; i++) {
                validateDataInternal(schema[KEYWORD_CONST_TYPE], dataArray[i]);
            }
        }
        
        /**
         * Validates a JSON array that is defining the different types in a
         * staticly-sized array. This validates that each index in the JSON 
         * array is a JSON object defining a type.
         * 
         * @param obj The JSON array to validate.
         */
        function validateSchemaConstLengthArray(obj) {
            var i
              , field
              , constLength = obj[KEYWORD_CONST_LENGTH]
              , constLengthType;
            
            // Ensure that the sub-schema exists.
            if (constLength === null) {
                throw "The sub-schema for a constant length array is null: " +
                        JSON.stringify(obj);
            }
            // Ensure that the sub-schema is an array.
            constLengthType = Object.prototype.toString.call(constLength);
            if (constLengthType !== JS_TYPE_ARRAY) {
                throw "The sub-schema for a constant length array, '" +
                        constLengthType +
                        "', is not of " +
                		"type '" +
                		JS_TYPE_ARRAY +
                		"': " +
                		JSON.stringify(obj);
            }
            
            // Validate each index in the array.
            for (i = 0; i < constLength.length; i++) {
                // Get the field.
                field = constLength[i];
                
                // If the index is null, throw an exception.
                if (field === null) {
                    throw "The element at index " + 
                            i + 
                            " is null: " + 
                            JSON.stringify(obj);
                }
                
                // If the index is not an object, throw an exception.
                if (Object.prototype.toString.call(field) !== JS_TYPE_OBJECT) {
                    throw "The element at index " + 
                            i + 
                            "is not a JSON object: " + 
                            JSON.stringify(obj);
                }
                
                // Validate this field's type.
                validateSchemaInternal(field);
            }
        }
        
        /**
         * Validates that the data array is the same length as is specified in
         * the schema and that each element in the data array has the type 
         * defined in the schema array at the same index.
         * 
         * @param schema A JSON array where each index defines a type.
         * 
         * @param dataArray The JSON array whose indicies are to be validated.
         */
        function validateDataConstLengthArray(schema, dataArray) {
            var i
              , constLength = schema[KEYWORD_CONST_LENGTH];
            
            // As a quick check, ensure both arrays are the same length. Even
            // if the schema array lists its final elements as being optional
            // and the data array is short those entries, it is still 
            // considered invalid. Instead, the data array should be prepended
            // with 'null's to match the schema's length.
            if (constLength.length !== dataArray.length) {
                throw "The schema array and the data array are of different " +
                        "lengths: " +
                        JSON.stringify(dataArray);
            }
            
            // For each schema in the schema array, ensure that the 
            // corresponding element in the data array is of the correct type.
            for (i = 0; i < schema.length; i++) {
                validateDataInternal(constLength[i], dataArray[i]);
            }
        }
        
        /**
         * Validates a JSON object by checking for the "doc" and "optional" 
         * tags. Any combination of the tags may be given. 
         * 
         * The "doc" tag must be a string.
         * 
         * The "optional" tag must be a boolean.
         * 
         * @param obj The type object to check for options.
         */
        function validateSchemaOptions(obj) {
            var doc = obj[KEYWORD_DOC]
              , docType = typeof doc
              , optional = obj[KEYWORD_OPTIONAL]
              , optionalType = typeof optional;
            
            if ((docType !== "undefined") && 
                (Object.prototype.toString.call(doc) !== JS_TYPE_STRING)) {
                
                throw "The 'doc' field's value must be of type string: " + 
                        JSON.stringify(obj);
            }
            
            if ((optionalType !== "undefined") && 
                (Object.prototype.toString.call(optional) !== JS_TYPE_BOOLEAN)) {
                
                throw "The 'optional' field's value must be of type boolean: " + 
                        JSON.stringify(obj);
            }
        }
        
        /**
         * Validates a JSON object based on its "type" field. If the object 
         * doesn't have a "type" field, it is invalid. The list of valid types
         * are, "boolean", "number", "string", "object", and "array".
         * 
         * @param obj The object whose "type" field will be evaluated and used 
         *            to continue validation.
         * 
         * @see validateSchemaBoolean(object)
         * @see validateSchemaNumber(object)
         * @see validateSchemaString(object)
         * @see validateSchemaObject(object)
         * @see validateSchemaArray(object)
         */
        function validateSchemaInternal(obj) {
            var type = obj[KEYWORD_TYPE]
              , typeType;
            
            if (type === null) {
                throw "The '" + 
                        KEYWORD_TYPE + 
                        "' field cannot be null: " + 
                        JSON.stringify(obj);
            }
            typeType = typeof type;
            if (typeType === "undefined") {
                // Attempt to get the remote schema.
                storeRemoteSchema(obj);

                // If a remote schema was not added, then there is no
                // definition for this object.
                if (typeof obj[KEYWORD_CONCORDIA] === "undefined") {

                    throw "The '" + 
                            KEYWORD_TYPE + 
                            "' field is missing: " + 
                            JSON.stringify(obj);
                }
            }
            else if (Object.prototype.toString.call(type) !== JS_TYPE_STRING) {
                throw "The '" + 
                        KEYWORD_TYPE + 
                        "' field is not a string: " + 
                        JSON.stringify(obj);
            }
            else if (type === TYPE_BOOLEAN) {
                validateSchemaBoolean(obj);
            }
            else if (type === TYPE_NUMBER) {
                validateSchemaNumber(obj);
            }
            else if (type === TYPE_STRING) {
                validateSchemaString(obj);
            }
            else if (type === TYPE_OBJECT) {
                validateSchemaObject(obj);
            }
            else if (type === TYPE_ARRAY) {
                validateSchemaArray(obj);
            }
            else {
                throw "Type unknown: " + type;
            }
        
            validateSchemaOptions(obj);
        }
        
        /**
         * Validates that one schema extends an 'original' schema. This means
         * that all data-definition fields must be equal, e.g. types, object
         * field names, etc. It is assumed that the two schemas have already
         * been validated. If the extender does not fully extend the original,
         * an exception is thrown. There is no return value.
         * 
         * @param original The original schema which is being extended.
         * 
         * @param extender The schema that is extending the original.
         */
        function validateSchemaExtender(original, extender) {
            var type = original[KEYWORD_TYPE];
            
            if (type === TYPE_BOOLEAN) {
                validateSchemaExtenderBoolean(original, extender);
            }
            else if (type === TYPE_NUMBER) {
                validateSchemaExtenderNumber(original, extender);
            }
            else if (type === TYPE_STRING) {
                validateSchemaExtenderString(original, extender);
            }
            else if (type === TYPE_OBJECT) {
                validateSchemaExtenderObject(original, extender);
            }
            else if (type === TYPE_ARRAY) {
                validateSchemaExtenderArray(original, extender);
            }
            
            validateSchemaOptionsExtender(original, extender);
        }
        
        /**
         * Passes the data to the appropriate validator based on the schema.
         * 
         * @param schema The schema to use to validate the data.
         * 
         * @param data The data to validate.
         * 
         * @see validateDataBoolean(object, object)
         * @see validateDataNumber(object, object)
         * @see validateDataString(object, object)
         * @see validateDataObject(object, object)
         * @see validateDataArray(object, object)
         */
        function validateDataInternal(schema, data) {
            var type = schema[KEYWORD_TYPE];
            
            if (type === TYPE_BOOLEAN) {
                validateDataBoolean(schema, data);
            }
            else if (type === TYPE_NUMBER) {
                validateDataNumber(schema, data);
            }
            else if (type === TYPE_STRING) {
                validateDataString(schema, data);
            }
            else if (type === TYPE_OBJECT) {
                validateDataObject(schema, data);
            }
            else if (type === TYPE_ARRAY) {
                validateDataArray(schema, data);
            }
            else {
                schema[KEYWORD_CONCORDIA].validateData(data);
            }
        }
        
        /**
         * Validates the "option"s fields of a schema that is being extended by
         * another schema.
         * 
         * @param original The original schema that is being extended.
         * 
         * @param extender The schema that is extending the original schema.
         */
        function validateSchemaOptionsExtender(original, extender) {
            // Get the optional field from the original schema.
            var optional = original[KEYWORD_OPTIONAL];
            
            // If the original schema did not define an optional value or if it
            // explicitly states that the field is not optional, then the
            // extender cannot override this and make it optional.
            if ((
                    (typeof optional === "undefined") ||
                    (! optional)) &&
                (extender[KEYWORD_OPTIONAL])) {
                
                throw "The original schema did not allow a field to be " +
                		"optional, but the extending schema does: " +
                		JSON.stringify(extender);
            }
        }
        
        /**
         * Takes in a valid JSON object and validates that it conforms to the 
         * Concordia schema specification.
         * 
         * @param obj The JSON object to validate.
         * 
         * @return The given JSON object that represents a valid schema.
         * 
         * @throws The schema is not valid.
         */
        function validateSchema(obj) {
            var type = obj[KEYWORD_TYPE]
              , typeType
              , optional = obj[KEYWORD_OPTIONAL]
              , optionalType = typeof optional;
            
            if (type === null) {
                throw "The root object's '" + 
                        KEYWORD_TYPE + 
                        "' field cannot be null: " + 
                        JSON.stringify(obj);
            }
            typeType = typeof type;
            if (typeType === "undefined") {
                throw "The root object's '" +
                        KEYWORD_TYPE +
                        "' field is missing: " + 
                        JSON.stringify(obj);
            }
            if (Object.prototype.toString.call(type) !== JS_TYPE_STRING) {
                throw "The root object's '" +
                        KEYWORD_TYPE +
                        "' field must be a string: " +
                        JSON.stringify(obj);
            }
            if ((type !== TYPE_OBJECT) && (type !== TYPE_ARRAY)) {
                throw "The root object's '" +
                        KEYWORD_TYPE +
                        "' field must either be " +
                        "'object' or 'array': " + 
                        JSON.stringify(obj);
            }
            
            if ((optionalType !== "undefined") && optional) {
                throw "The 'optional' field is not allowed at the root of " +
                        "the definition.";
            }
            
            validateSchemaInternal(obj);

            return obj;
        }
        
        /**
         * Validate data against any schema, even a partial one. This is used
         * internally to allow checking of subcomponents without worrying about
         * the specifics of the data.
         * 
         * @param data Any data to validate.
         * 
         * @return The same data as it was passed in.
         */
        function validateDataNoCheck(data) {
            // Validate the data using this object's sub-schema.
            validateDataInternal(concordia[KEYWORD_SCHEMA], data);
            
            // Returns the data just to conform to the function it is
            // shadowing, {@link #validateData(data)}.
            return data;
        }
        
        /**
         * Validates that this schema extends some other schema. If it does
         * not, an exception is thrown. If it does nothing is returned.
         * 
         * @param schema The schema that this Concoria object must conform to.
         *               This may be an already-created Concordia object or a
         *               string that represents one, which will first be
         *               evaluated.
         */
        concordia.conformsTo = function (schema) {
            if (schema === null) {
                throw "The schema is null.";
            }
            
            // Get the original schema and attempt to convert it if it isn't
            // already a Concordia object.
            var original = schema;
            if (! (schema instanceof Concordia)) {
                original = new Concordia(schema);
            }
            
            // Use the internal function to validate that 
            validateSchemaExtender(
                original[KEYWORD_SCHEMA],
                concordia[KEYWORD_SCHEMA]);
        };
        
        /**
         * Validate data against this object's schema.
         * 
         * @param data The data to validate against the schema, which must be
         *             valid JSON.
         * 
         * @return The data that has been validated and now has a
         *          language-level representation.
         */
        concordia.validateData = function (data) {
            var jsonData = data
              , jsonDataType = Object.prototype.toString.call(jsonData);
            
            // If the data is a string, attempt to convert it into an object or
            // an array.
            if (jsonDataType === JS_TYPE_STRING) {
                jsonData = JSON.parse(data);
                jsonDataType = Object.prototype.toString.call(jsonData);
            }
            
            // The type of the data must be either an object or an array.
            if ((jsonDataType === JS_TYPE_ARRAY) ||
                (jsonDataType === JS_TYPE_OBJECT)) {
                
                validateDataInternal(concordia[KEYWORD_SCHEMA], jsonData);
            }
            else {
                throw "The data must either be a JSON object or a JSON " +
                        "array or a string representing one of the two.";
            }
            
            return jsonData;
        };
        
        // Validate that the schema is valid.
        var schemaJson = schema;
        var schemaType = Object.prototype.toString.call(schema);
        // If we are given a string representing the schema, first convert it
        // into JSON.
        if (schemaType === JS_TYPE_STRING) {
            schemaJson = JSON.parse(schema);
            schemaType = Object.prototype.toString.call(schemaJson);
        }
        
        // If it isn't a JSON object, then throw an exception.
        if (schemaType !== JS_TYPE_OBJECT) {
            throw "The schema must either be a JSON object or a string " +
                    "representing a JSON object.";
        }
        
        // Validate the schema and, if it passes, store it as the schema.
        concordia[KEYWORD_SCHEMA] = validateSchema(schemaJson);

    // End of Concordia definition.
    }(this));
}