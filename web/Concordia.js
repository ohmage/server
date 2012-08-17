/**
 * The Concordia object has one constructor which takes a schema and validates
 * it. If it fails validation, an exception will be thrown.
 * 
 * There is one public function, which is validateData(data). 'data' must be a
 * JSON object or a JSON array or a string representing one of the two. If the
 * data is not valid, an exception will be thrown; otherwise, the function will 
 * return the valid JSON object or JSON array.
 * 
 * @author John Jenkins
 */
function Concordia(schema) {
	'use strict';
	
	(function (concordia) {
		// Concordia key words.
		var KEYWORD_TYPE = "type"
		  , KEYWORD_OPTIONAL = "optional"
		  , KEYWORD_DOC = "doc"
		  , KEYWORD_SCHEMA = "schema"
		  , KEYWORD_NAME = "name"
		
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
		  , JS_TYPE_ARRAY = "[object Array]";
		
		// Predefine the recursive functions to allow them to be referenced
		// before they are defined.
		function validateSchemaType(obj) {}
		function validateDataType(schema, data) {}
		
		/**
		 * Validates a JSON object whose "type" has already been determined to
		 * be "boolean". There are no other requirements for the "boolean" 
		 * type.
		 * 
		 * @param obj The JSON object to validate.
		 */
		function validateSchemaBooleanType(obj) {
			// There are no additional requirements for a boolean type.
		}
		
		/**
		 * Validates that the data is a boolean or, if it is 'null' or missing,
		 * that hte field is optional.
		 * 
		 * @param schema The Schema to use to validate the data.
		 * 
		 * @param data The data to validate.
		 */
		function validateDataBooleanType(schema, data) {
			// If the data is not present or 'null', ensure that it is 
			// optional.
			if (data === null) {
				if (! schema[KEYWORD_OPTIONAL]) {
					throw "The data is null and not optional.";
				}
			}
			// If the data is present, ensure that it is a boolean.
			else if (Object.prototype.toString.call(data) !== JS_TYPE_BOOLEAN) {
				throw "The value is not a boolean: " +
						JSON.stringify(data, null, null);
			}
		}
		
		/**
		 * Validates a JSON object whose "type" has already been determined to
		 * be "number". There are no other requirements for the "number" type.
		 * 
		 * @param obj The JSON object to validate.
		 */
		function validateSchemaNumberType(obj) {
		    // There are no additional requirements for a number type.
		}
		
		/**
		 * Validates that the data is a number or, if it is 'null' or missing,
		 * that the field is optional. 
		 * 
		 * @param schema The schema to use to validate the data.
		 * 
		 * @param data The data to validate.
		 */
		function validateDataNumberType(schema, data) {
			// If the data is not present or 'null', ensure that it is
			// optional.
			if (data === null) {
				if (! schema[KEYWORD_OPTIONAL]) {
					throw "The data is null and not optional.";
				}
			}
			// If the data is present, ensure that it is a number.
			else if (Object.prototype.toString.call(data) !== JS_TYPE_NUMBER) {
				throw "The value is not a number: " + 
						JSON.stringify(data, null, null);
			}
		}
		
		/**
		 * Validates a JSON object whose "type" has already been determined to
		 * be "string". There are no other requirements for the "string" type.
		 * 
		 * @param obj The JSON object to validate.
		 */
		function validateSchemaStringType(obj) {
		    // There are no additional requirements for a string type.
		}
		
		/**
		 * Validates that the data is a string or, if it is 'null' or missing,
		 * that the field is optional.
		 */
		function validateDataStringType(schema, data) {
			// If the data is not present or 'null', ensure that it is 
			// optional.
			if (data === null) {
				if (! schema[KEYWORD_OPTIONAL]) {
					throw "The data is null and not optional.";
				}
			}
			// If the data is present, ensure that it is a string.
			else if (Object.prototype.toString.call(data) !== JS_TYPE_STRING) {
				throw "The data is not a string: " + 
						JSON.stringify(data, null, null);
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
		function validateSchemaObjectType(obj) {
		    var schema = obj[KEYWORD_SCHEMA]
		      , schemaType
		      , i
		      , field
		      , name
		      , nameType;
		    
		    // Verify the schema isn't null.
		    if (schema === null) {
		        throw "The '" + KEYWORD_SCHEMA + "' field's value is null: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    // Verify that the schema is present and is a JSON array.
		    schemaType = typeof schema;
		    if (schemaType === "undefined") {
		        throw "The '" + KEYWORD_SCHEMA + "' field is missing: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    if (Object.prototype.toString.call(schema) !== JS_TYPE_ARRAY) {
		        throw "The '" +
		        		KEYWORD_SCHEMA +
		        		"' field's value must be a JSON array: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    // For each of the JSON objects, verify that it has a name and a
		    // type.
		    for (i = 0; i < schema.length; i += 1) {
		        field = schema[i];
		    	// Verify that the index isn't null.
		        if (field === null) {
		            throw "The element at index " + 
		            		i + 
		            		" of the '" +
		            		KEYWORD_SCHEMA +
		            		"' field is null: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        // Verify that the index is a JSON object and not an array.
		        if (Object.prototype.toString.call(field) !== JS_TYPE_OBJECT) {
		            throw "The element at index " + 
		            		i + 
		            		" of the '" +
		            		KEYWORD_SCHEMA +
		            		"' field is not a JSON object: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        
		        // Verify that the JSON object contains a "name" field and that
		        // it's not null.
		        name = field[KEYWORD_NAME];
		        if (name === null) {
		            throw "The '" +
		            		KEYWORD_NAME +
		            		"' field for the JSON object at index " + 
		            		i + 
		            		" is null: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        // Verify that the "name" field exists and is a string.
		        nameType = typeof name;
		        if (name === "undefined") {
		        	throw "The '" +
		        			KEYWORD_NAME +
		        			"' field for the JSON object at index " + 
		        			i + 
		        			" is misisng: " + 
		        			JSON.stringify(obj, null, null);
		        }
		        if (Object.prototype.toString.call(name) !== JS_TYPE_STRING) {
		            throw "The type of the '" +
		            		KEYWORD_NAME +
		            		"' field for the JSON object at index " + 
		            		i + 
		            		" is not a string: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        
		        // Validates the type of this field.
		        validateSchemaType(field);
		    }
		}
		
		/**
		 * Validates that the data is a JSON object or, if the data is 'null'
		 * or missing, that it is optional.
		 * 
		 * @param schema The schema to use to validate the data.
		 * 
		 * @param data The data to validate.
		 */
		function validateDataObjectType(schema, data) {
			var i
			  , schemaFields
			  , schemaField
			  , name
			  , dataField
			  , dataFieldType;
			
			// If the data is not present or 'null', ensure that it is 
			// optional.
			if (data === null) { 
				if (! schema[KEYWORD_OPTIONAL]) {
					throw "The data is not optional.";
				}
				else {
					return;
				}
			}
			
			// Ensure that it is an object.
			if (Object.prototype.toString.call(data) !== JS_TYPE_OBJECT) {
				throw "The data is not a JSON object: " + 
						JSON.stringify(data, null, null);
			}
			
			// For each index in the object's "schema" field,
			schemaFields = schema[KEYWORD_SCHEMA];
			for (i = 0; i < schemaFields.length; i += 1) {
				// Get this index, which is a JSON object that contains the
				// type schema.
				schemaField = schemaFields[i];
				
				// Get the name.
				name = schemaField[KEYWORD_NAME];
				
				// Verify that the field exists in the data or that it is
				// optional.
				dataField = data[name];
				dataFieldType = typeof dataField;
				if (dataFieldType === "undefined") { 
					if(! schemaField[KEYWORD_OPTIONAL]) {
						throw "The field '" +
								name +
								"' is missing from the data: " +
								JSON.stringify(data, null, null);
					}
				}
				else {
					// Verify that the type of the value of that field in the 
					// data matches the schema.
					validateDataType(schemaField, dataField);
				}
			}
		}
		
		/**
		 * Validates a JSON object whose "type" has already been determined to
		 * be "array". The "array" type requires a "schema" field whose value
		 * is either a JSON object or a JSON array. If it is a JSON object,
		 * that signifies that a data point could have any number of indicies,
		 * but that they are all the same type. If it is a JSON array, that
		 * signifies that a data point will have the exact same number of 
		 * indicies as the number of indicies in this array and that the type
		 * of each index in the data array must be the specified in the 
		 * correlating index in this array.
		 * 
		 * @param obj The JSON object to validate.
		 * 
		 * @see validateConstLengthArray(object)
		 * @see validateConstTypeArray(object)
		 */
		function validateSchemaArrayType(obj) {
		    var schema = obj[KEYWORD_SCHEMA]
		      , schemaType
		      , schemaJsType;

			// Validate that the schema is not null.
		    if (schema === null) {
		        throw "The '" +
		        		KEYWORD_SCHEMA +
		        		"' field's value cannot be null: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    // Validate that the schema exists and that it is an object, either 
		    // a JSON object or a JSON array.
		    schemaType = typeof schema;
		    if (schemaType === "undefined") {
		        throw "The '" +
		        		KEYWORD_SCHEMA +
		        		"' field is missing: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    schemaJsType = Object.prototype.toString.call(schema);
		    // If it is an array, this is a definition for a data point whose
		    // value will be a constant length array, and each index in that
		    // array must have a defined type. But, the types may vary from
		    // index to index.
		    if (schemaJsType === JS_TYPE_ARRAY) {
		        validateSchemaConstLengthArray(schema);
		    }
		    // If it is an object, this is a definition for a data point whose 
		    // value will be a variable length array, but each index's type
		    // must be the same.
		    else if(schemaJsType === JS_TYPE_OBJECT) {
		        validateSchemaConstTypeArray(schema);
		    }
		    // Otherwise, it is invalid.
		    else {
		    	throw "The '" +
		    			KEYWORD_SCHEMA +
		    			"' field's type must be either an array or an object: " + 
        				JSON.stringify(obj, null, null);
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
		function validateDataArrayType(schema, data) {
			var arraySchema;
			
			// If the data is not present or 'null', ensure that it is
			// optional.
			if (data === null) { 
				if (! schema[KEYWORD_OPTIONAL]) {
					throw "The data is not optional.";
				}
				else {
					return;
				}
			}
			
			// Ensure it is an array.
			if (Object.prototype.toString.call(data) !== JS_TYPE_ARRAY) {
				throw "The data is not a JSON array: " + 
						JSON.stringify(data, null, null);
			}
			
			// Get the schema.
			arraySchema = schema[KEYWORD_SCHEMA];
			// If it's an array, then pass it to the constant length array
			// validator.
			if (Object.prototype.toString.call(arraySchema) === JS_TYPE_ARRAY) {
				validateDataConstLengthArray(arraySchema, data);
			}
			// If it's an object, then pass it to the constant type array
			// validator.
			else {
				validateDataConstTypeArray(arraySchema, data);
			}
		}
		
		/**
		 * Validates a JSON array that is defining the different types in a
		 * staticly-sized array. This validates that each index in the JSON 
		 * array is a JSON object defining a type.
		 * 
		 * @param arr The JSON array to validate.
		 */
		function validateSchemaConstLengthArray(arr) {
			var i
			  , field;
			
			// Validate each index in the array.
		    for (i = 0; i < arr.length; i++) {
		        field = arr[i];
		        
		        // If the index is null, throw an exception.
		        if (field === null) {
		            throw "The element at index " + 
		            		i + 
		            		" is null: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        
		        // If the index is not an object, throw an exception.
		        if (Object.prototype.toString.call(field) !== JS_TYPE_OBJECT) {
		            throw "The element at index " + 
		            		i + 
		            		"is not a JSON object: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        
		        validateSchemaType(field);
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
			var i;
			
			// As a quick check, ensure both arrays are the same length. Even
			// if the schema array lists its final elements as being optional
			// and the data array is short those entries, it is still 
			// considered invalid. Instead, the data array should be prepended
			// with 'null's to match the schema's length.
			if (schema.length !== dataArray.length) {
				throw "The schema array and the data array are of different lengths: " +
						JSON.stringify(dataArray, null, null);
			}
			
			// For each schema in the schema array, ensure that the 
			// corresponding element in the data array is of the correct type.
			for (i = 0; i < schema.length; i++) {
				validateDataType(schema[i], dataArray[i]);
			}
		}
		
		/**
		 * Validates a JSON object that is defining the type for all of the 
		 * indicies in a JSON array.
		 * 
		 * @param obj The JSON object to validate.
		 */
		function validateSchemaConstTypeArray(obj) {
		    validateSchemaType(obj);
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
				validateDataType(schema, dataArray[i]);
			}
		}
		
		/**
		 * Validates a JSON object based on its "type" field. If the object 
		 * doesn't have a "type" field, it is invalid. The list of valid types
		 * are, "number", "string", "object", and "array".
		 * 
		 * @param obj The object whose "type" field will be evaluated and used 
		 * 			  to continue validation.
		 * 
		 * @see validateSchemaNumberType(object)
		 * @see validateSchemaStringType(object)
		 * @see validateSchemaObjectType(object)
		 * @see validateSchemaArrayType(object)
		 */
		function validateSchemaType(obj) {
		    var type = obj[KEYWORD_TYPE]
		      , typeType;
		    
		    if (type === null) {
		    	throw "The '" + KEYWORD_TYPE + "' field cannot be null: " + 
		    			JSON.stringify(obj, null, null);
		    }
		    typeType = typeof type;
		    if (typeType === "undefined") {
		    	throw "The '" + KEYWORD_TYPE + "' field is missing: " + 
		    			JSON.stringify(obj, null, null);
		    }
		    if (Object.prototype.toString.call(type) !== JS_TYPE_STRING) {
		    	throw "The '" + KEYWORD_TYPE + "' field is not a string: " + 
    					JSON.stringify(obj, null, null);
		    }
		    
		    if (type === TYPE_BOOLEAN) {
		    	validateSchemaBooleanType(obj);
		    }
		    else if (type === TYPE_NUMBER) {
		        validateSchemaNumberType(obj);
		    }
		    else if (type === TYPE_STRING) {
		        validateSchemaStringType(obj);
		    }
		    else if (type === TYPE_OBJECT) {
		    	validateSchemaObjectType(obj);
		    }
		    else if (type === TYPE_ARRAY) {
		    	validateSchemaArrayType(obj);
		    }
		    else {
		        throw "Type unknown: " + type;
		    }
		
		    validateSchemaTypeOptions(obj);
		}
		
		/**
		 * Passes the data to the appropriate validator based on the schema.
		 * 
		 * @param schema The schema to use to validate the data.
		 * 
		 * @param data The data to validate.
		 * 
		 * @see validateDataNumberType(object, object)
		 * @see validateDataStringType(object, object)
		 * @see validateDataObjectType(object, object)
		 * @see validateDataArrayType(object, object)
		 */
		function validateDataType(schema, data) {
			var type = schema[KEYWORD_TYPE];
			
			if (type === TYPE_BOOLEAN) {
				validateDataBooleanType(schema, data);
			}
			else if (type === TYPE_NUMBER) {
				validateDataNumberType(schema, data);
			}
			else if (type === TYPE_STRING) {
				validateDataStringType(schema, data);
			}
			else if (type === TYPE_OBJECT) {
				validateDataObjectType(schema, data);
			}
			else if (type === TYPE_ARRAY) {
				validateDataArrayType(schema, data);
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
		function validateSchemaTypeOptions(obj) {
		    var doc = obj[KEYWORD_DOC]
		      , docType = typeof doc
		      , optional = obj[KEYWORD_OPTIONAL]
		      , optionalType = typeof optional;
		    
		    if ((docType !== "undefined") && 
		    		(Object.prototype.toString.call(doc) !== JS_TYPE_STRING)) {
		    	
		        throw "The 'doc' field's value must be of type string: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    if ((optionalType !== "undefined") && 
		    		(Object.prototype.toString.call(optional) !== JS_TYPE_BOOLEAN)) {
		    	
		        throw "The 'optional' field's value must be of type boolean: " + 
		        		JSON.stringify(obj, null, null);
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
		      , optionalType = typeof obj[KEYWORD_OPTIONAL];
		    
		    if (type === null) {
		    	throw "The root object's '" + 
		    			KEYWORD_TYPE + 
		    			"' field cannot be null: " + 
		    			JSON.stringify(obj, null, null);
		    }
		    typeType = typeof type;
		    if (typeType === "undefined") {
		    	throw "The root object's '" +
		    			KEYWORD_TYPE +
		    			"' field is missing: " + 
		    			JSON.stringify(obj, null, null);
		    }
		    if (Object.prototype.toString.call(type) !== JS_TYPE_STRING) {
		    	throw "The root object's '" +
		    			KEYWORD_TYPE +
		    			"' field must be a string: " +
		    			JSON.stringify(obj, null, null);
		    }
		    if ((type !== TYPE_OBJECT) && (type !== TYPE_ARRAY)) {
		        throw "The root object's '"
		        		KEYWORD_TYPE +
		        		"' field must either be " +
		        		"'object' or 'array': " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    if (optionalType !== "undefined") {
		    	throw "The 'optional' field is not allowed at the root of the definition.";
		    }
		    
		    validateSchemaType(obj);

		    return obj;
		}
		
		/**
		 * Validate data against this object's schema.
		 * 
		 * @param obj The data to validate against the schema. This must either
		 * 			  be a JSON object or a JSON array or a string representing 
		 * 			  one of the two.
		 */
		concordia.validateData = function (data) {
			var jsonData = data
			  , jsonDataType = Object.prototype.toString.call(jsonData);
			if (jsonDataType === JS_TYPE_STRING) {
				jsonData = JSON.parse(data);
				jsonDataType = Object.prototype.toString.call(jsonData);
			}
			
			// The type
			if (jsonDataType === JS_TYPE_OBJECT) {
				validateDataType(concordia[KEYWORD_SCHEMA], jsonData);
			}
			else {
				throw "The data must either be a JSON object or a JSON array or a string representing one of the two.";
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
			throw "The schema must either be a JSON object or a string representing a JSON object.";
		}
		
		// Validate the schema and, if it passes, store it as the schema.
		concordia[KEYWORD_SCHEMA] = validateSchema(schemaJson);

	// End of Concordia definition.
	}(this));
}