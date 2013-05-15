package org.ohmage.domain;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.ohmage.exception.DomainException;

public class Audio {
	public final UUID id;
	public final String type;
	public final InputStream content;

	public Audio(
		final UUID id,
		final String type,
		final byte[] content)
		throws DomainException {
		
		// Validate the ID.
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		else {
			this.id = id;
		}
		
		// Validate the type.
		if(type == null) {
			throw new DomainException("The type is null.");
		}
		else {
			this.type = type;
		}
		
		// Validate the content.
		if(content == null) {
			throw new DomainException("The content is null.");
		}
		else if(content.length == 0) {
			throw new DomainException("The content is empty.");
		}
		else {
			this.content = new ByteArrayInputStream(content);
		}
	}
	
	public UUID getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
	
	public InputStream getContentStream() {
		return content;
	}
}