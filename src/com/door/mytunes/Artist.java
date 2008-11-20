package com.door.mytunes;

import com.u2d.model.AbstractComplexEObject;
import com.u2d.model.Title;
import com.u2d.type.atom.StringEO;
import com.u2d.type.atom.TextEO;
import javax.persistence.Entity;

/**
 *
 * @author jfruncek
 */
@Entity
public class Artist extends AbstractComplexEObject {

    private final StringEO _name = new StringEO();
    private final TextEO _bio = new TextEO();
    public static final String[] fieldOrder = {"name", "bio"};

    public Artist() {
    }

    public StringEO getName() {
        return _name;
    }

    public TextEO getBio() {
        return _bio;
    }

    public Title title() {
        return _name.title();
    }
}
