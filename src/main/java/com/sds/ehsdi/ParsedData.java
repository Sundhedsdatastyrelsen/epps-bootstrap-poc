package com.sds.ehsdi;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParsedData {

    private Map<String, List<String>> attributes; 
    private List<String> permissions; 

    public ParsedData() {
        this.attributes = new HashMap<>();
        this.permissions = new ArrayList<>();
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }
    
    public void addAttribute(String name, List<String> values) {
        attributes.put(name, values);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }
    
}
