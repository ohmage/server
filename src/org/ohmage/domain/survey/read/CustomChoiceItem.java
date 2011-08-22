package org.ohmage.domain.survey.read;

/**
 * Container for custom choices for serializing into a "global"
 * choice_glossary.
 * 
 * @author Joshua Selsky
 */
public class CustomChoiceItem {
	
	private int _id;
	private int _originalId;
	private String _username;
	private String _label;
	private String _type;
	
	public CustomChoiceItem(int originalId, String username, String label, String type) {
		_originalId = originalId;
		_username = username;
		_label = label;
		_type = type;
	}

	public int getId() {
		return _id;
	}
	
	public void setId(int id) {
		_id = id;
	}
	
	public int getOriginalId() {
		return _originalId;
	}
	
	public String getUsername() {
		return _username;
	}

	public String getLabel() {
		return _label;
	}

	public String getType() {
		return _type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_label == null) ? 0 : _label.hashCode());
		result = prime * result + _originalId;
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		result = prime * result
				+ ((_username == null) ? 0 : _username.hashCode());
		return result;
	}

	@Override
	/**
	 * Note that "id" is not used as part of the equality check because the original id ("key") 
	 * is part of the real composite key. The "id" value is unique across a set of survey 
	 * response output.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomChoiceItem other = (CustomChoiceItem) obj;
		if (_label == null) {
			if (other._label != null)
				return false;
		} else if (!_label.equals(other._label))
			return false;
		if (_originalId != other._originalId)
			return false;
		if (_type == null) {
			if (other._type != null)
				return false;
		} else if (!_type.equals(other._type))
			return false;
		if (_username == null) {
			if (other._username != null)
				return false;
		} else if (!_username.equals(other._username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CustomChoiceItem [_id=" + _id + ", _originalId=" + _originalId
				+ ", _username=" + _username + ", _label=" + _label
				+ ", _type=" + _type + "]";
	}
}
