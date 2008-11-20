package com.door.mytunes;

import com.u2d.type.atom.ImgEO;
import com.u2d.model.AbstractComplexEObject;
import com.u2d.model.Title;
import com.u2d.type.atom.StringEO;
import javax.persistence.Entity;

/**
 *
 * @author jfruncek
 */
@Entity
public class Album extends AbstractComplexEObject {

    private final StringEO _name = new StringEO();
    private final ImgEO _cover = new ImgEO();
    
    public static final String[] fieldOrder = {"name", "cover"};

    public Album() {
    }

    public StringEO getName() {
        return _name;
    }

    public ImgEO getCover() {
        return _cover;
    }

    public Title title() {
        return _name.title();
    }
}
