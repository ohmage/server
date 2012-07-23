/**
 * The Concordia object has one constructor which takes a schema and validates
 * it. If it fails validation, an exception will be thrown.
 * 
 * There is one public function, which is validate(data). 'data' must be a JSON
 * object or a JSON array or a string representing one of the two. If the data
 * is not valid, an exception will be thrown; otherwise, the function will 
 * return the valid JSON object or JSON array.
 * 
 * @author John Jenkins
 */
function Concorida(schema) {
	var Concordia = {};
	
	(function () {
		'use strict';
		
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
		    var type = obj["type"];
		    
		    if(type === null) {
		    	throw "The root object's 'type' field cannot be null: " + 
		    			JSON.stringify(obj, null, null);
		    }
		    if(typeof type === "undefined") {
		    	throw "The root object's 'type' field is missing: " + 
		    			JSON.stringify(obj, null, null);
		    }
		    if((type !== "object") && (type !== "array")) {
		        throw "The root object's 'type' field must either be " +
		        		"'object' or 'array': " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    if(typeof obj["optional"] !== "undefined") {
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
		Concordia.validateData = function (data) {
			var jsonData = data;
			if(typeof data === "string") {
				jsonData = JSON.parse(data);
			}
			
			// The type
			if(typeof jsonData === "object") {
				validateDataType(Concordia.schema, jsonData);
			}
			else {
				throw "The data must either be a JSON object or a JSON array or a string representing one of the two.";
			}
			
			return jsonData;
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
		    var type = obj["type"];
		    
		    if(type === "boolean") {
		    	validateSchemaBooleanType(obj);
		    }
		    else if(type === "number") {
		        validateSchemaNumberType(obj);
		    }
		    else if(type === "string") {
		        validateSchemaStringType(obj);
		    }
		    else if(type === "object") {
		    	validateSchemaObjectType(obj);
		    }
		    else if(type === "array") {
		    	validateSchemaArrayType(obj);
		    }
		    else if(type === null) {
		    	throw "The 'type' field cannot be null: " + 
		    			JSON.stringify(obj, null, null);
		    }
		    else if(typeof type === "undefined") {
		    	throw "The 'type' field is missing: " + 
		    			JSON.stringify(obj, null, null);
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
			var type = schema["type"];
			
			if(type === "boolean") {
				validateDataBooleanType(schema, data);
			}
			else if(type === "number") {
				validateDataNumberType(schema, data);
			}
			else if(type === "string") {
				validateDataStringType(schema, data);
			}
			else if(type === "object") {
				validateDataObjectType(schema, data);
			}
			else if(type === "array") {
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
		    var doc = obj["doc"];
		    var docType = typeof doc;
		    if((docType !== "undefined") && (docType !== "string")) {
		        throw "The 'doc' field's value must be of type string: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    var optional = obj["optional"];
		    var optionalType = typeof optional;
		    if((optionalType !== "undefined") && (optionalType !== "boolean")) {
		        throw "The 'optional' field's value must be of type boolean: " + 
		        		JSON.stringify(obj, null, null);
		    }
		}
		
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
			var dataType = typeof data;
			// If the data is not present or 'null', ensure that it is 
			// optional.
			if((data === null) || (dataType === "undefined")) {
				if(! schema["optional"]) {
					throw "The data is null and not optional.";
				}
			}
			else if(dataType !== "boolean") {
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
			var dataType = typeof data;
			// If the data is not present or 'null', ensure that it is
			// optional.
			if((data === null) || (dataType === "undefined")) {
				if(! schema["optional"]) {
					throw "The data is null and not optional.";
				}
			}
			// If the data is present, ensure that it is a number.
			else if(dataType !== "number") {
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
			var dataType = typeof data;
			// If the data is not present or 'null', ensure that it is 
			// optional.
			if((data === null) || (dataType === "undefined")) {
				if(! schema["optional"]) {
					throw "The data is null and not optional.";
				}
			}
			// If the data is present, ensure that it is a string.
			else if(dataType !== "string") {
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
		    // Get the schema and verify that it isn't null.
		    var schema = obj["schema"];
		    if(schema === null) {
		        throw "The 'schema' field's value cannot be null: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    // Verify that the schema is present and is a JSON object, not an 
		    // array.
		    var schemaType = typeof schema;
		    if(schemaType === "undefined") {
		        throw "The 'schema' field is missing: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    if((schemaType !== "object") || (! (schema instanceof Array))) {
		        throw "The 'schema' field's value must be a JSON array: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    // For each of the JSON objects, verify that it has a name and a
		    // type.
		    for(var i = 0; i < schema.length; i++) {
		    	// Verify that the index isn't null.
		        var field = schema[i];
		        if(field === null) {
		            throw "The element at index " + 
		            		i + 
		            		" of the 'schema' field is null: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        // Verify that the index is a JSON object and not an array.
		        var fieldType = typeof field;
		        if((fieldType !== "object") || (field instanceof Array)) {
		            throw "The element at index " + 
		            		i + 
		            		" of the 'schema' field is not a JSON object: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        
		        // Verify that the JSON object contains a "name" field and that
		        // it's not null.
		        var name = field["name"];
		        if(name === null) {
		            throw "The 'name' field for the JSON object at index " + 
		            		i + 
		            		" is null: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        // Verify that the "name" field exists and is a string.
		        var nameType = typeof name;
		        if(nameType === "undefined") {
		        	throw "The 'name' field for the JSON object at index " + 
		        			i + 
		        			" is misisng: " + 
		        			JSON.stringify(obj, null, null);
		        }
		        if(nameType !== "string") {
		            throw "The type of the 'name' field for the JSON object at index " + 
		            		i + 
		            		" is not a string: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        
		        // validatees the type of this field.
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
			var dataType = typeof data;
			// If the data is not present or 'null', ensure that it is 
			// optional.
			if((data === null) || (dataType === "undefined")) { 
				if(! schema["optional"]) {
					throw "The data is not optional.";
				}
			}
			// Ensure that it is an object.
			if((typeof data !== "object") || (data instanceof Array)) {
				throw "The data is not a JSON object: " + 
						JSON.stringify(data, null, null);
			}
			
			// For each key in the schema's "schema" field,
			var schemaFields = schema["schema"];
			for(var i = 0; i < schemaFields.length; i++) {
				// Get this field, which contains the type schema.
				var schemaField = schemaFields[i];
				
				// Get the name.
				var name = schemaField["name"];
				
				// Verify that the field exists in the data or that it is
				// optional.
				var dataField = data[name];
				if((typeof dataField === "undefined") && 
						(! (schemaField["optional"]))) {
					
					throw "The field '" +
							name +
							"' is missing from the data: " +
							JSON.stringify(data, null, null);
				}
				
				// Verify that the type of the value of that field in the data
				// matches the schema.
				validateDataType(schemaField, dataField)
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
			// Validate that the schema is not null.
		    var schema = obj["schema"];
		    if(schema === null) {
		        throw "The 'schema' field's value cannot be null: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    // Validate that the schema exists and that it is an object, either 
		    // a JSON object or a JSON array.
		    var schemaType = typeof schema;
		    if(schemaType === "undefined") {
		        throw "The 'schema' field is missing: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    if(schemaType !== "object") {
		        throw "The 'schema' field's type must be either an array or an object: " + 
		        		JSON.stringify(obj, null, null);
		    }
		    
		    // If it is an array, this is a definition for a data point whose
		    // value will be a constant length array, and each index in that
		    // array must have a defined type. But, the types may vary from
		    // index to index.
		    if(schema instanceof Array) {
		        validateSchemaConstLengthArray(schema);
		    }
		    // If it is an object, this is a definition for a data point whose 
		    // value will be a variable length array, but each index's type
		    // must be the same.
		    else {
		        validateSchemaConstTypeArray(schema);
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
			var dataType = typeof data;
			// If the data is not present or 'null', ensure that it is
			// optional.
			if((data === null) || (dataType === "undefined")) { 
				if(! schema["optional"]) {
					throw "The data is not optional.";
				}
			}
			// Ensure it is an array.
			if((dataType !== "object") || (! (data instanceof Array))) {
				throw "The data is not a JSON array: " + 
						JSON.stringify(data, null, null);
			}
			
			// Get the schema.
			var arraySchema = schema["schema"];
			// If it's an array, then pass it to the constant length array
			// validator.
			if(arraySchema instanceof Array) {
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
		    for(var i = 0; i < arr.length; i++) {
		        var field = arr[i];
		        if(field === null) {
		            throw "The element at index " + 
		            		i + 
		            		" is null: " + 
		            		JSON.stringify(obj, null, null);
		        }
		        var fieldType = typeof field;
		        if((fieldType !== "object") || (field instanceof Array)) {
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
			// As a quick check, ensure both arrays are the same length. Even
			// if the schema array lists its final elements as being optional
			// and the data array is short those entries, it is still 
			// considered invalid. Instead, the data array should be prepended
			// with 'null's to match the schema's length.
			if(schema.length !== dataArray.length) {
				throw "The schema array and the data array are of different lengths: " +
						JSON.stringify(dataArray, null, null);
			}
			
			// For each schema in the schema array, ensure that the 
			// corresponding element in the data array is of the correct type.
			for(var i = 0; i < schema.length; i++) {
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
			// For each element in the data array, make sure that it conforms
			// to the given schema.
			for(var i = 0; i < dataArray.length; i++) {
				validateDataType(schema, dataArray[i]);
			}
		} 
		
		// Validate that the schema is valid.
		var schemaJson = schema;
		var schemaType = typeof schema;
		// If we are given a string representing the schema, first convert it
		// into JSON.
		if(schemaType === "string") {
			schemaJson = JSON.parse(schema);
		}
		// If it isn't a string or a JSON object, then throw an exception.
		else if((schemaType !== "object") || (schema instanceof Array)) {
			throw "The schema must either be a JSON object or a string representing a JSON object.";
		}
		
		// Validate the schema and, if it passes, store it as the schema.
		Concordia.schema = validateSchema(schemaJson);

	// End of Concordia definition.
	}());
	
	return Concordia;
}