//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tsinling.rxpermission3;


import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;

public class Permission {
    public final String name;
    public final boolean granted;
    public final boolean shouldShowRequestPermissionRationale;

    public Permission(String name, boolean granted) {
        this(name, granted, false);
    }

    public Permission(String name, boolean granted, boolean shouldShowRequestPermissionRationale) {
        this.name = name;
        this.granted = granted;
        this.shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale;
    }

    public Permission(List<Permission> permissions) {
        this.name = this.combineName(permissions);
        this.granted = this.combineGranted(permissions);
        this.shouldShowRequestPermissionRationale = this.combineShouldShowRequestPermissionRationale(permissions);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Permission that = (Permission)o;
            if (this.granted != that.granted) {
                return false;
            } else {
                return this.shouldShowRequestPermissionRationale != that.shouldShowRequestPermissionRationale ? false : this.name.equals(that.name);
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + (this.granted ? 1 : 0);
        result = 31 * result + (this.shouldShowRequestPermissionRationale ? 1 : 0);
        return result;
    }

    public String toString() {
        return "Permission{name='" + this.name + '\'' + ", granted=" + this.granted + ", shouldShowRequestPermissionRationale=" + this.shouldShowRequestPermissionRationale + '}';
    }

    private String combineName(List<Permission> permissions) {
        return ((StringBuilder) Observable.fromIterable(permissions).map(new Function<Permission, String>() {
            public String apply(Permission permission) throws Exception {
                return permission.name;
            }
        }).collectInto(new StringBuilder(), new BiConsumer<StringBuilder, String>() {
            public void accept(StringBuilder s, String s2) throws Exception {
                if (s.length() == 0) {
                    s.append(s2);
                } else {
                    s.append(", ").append(s2);
                }

            }
        }).blockingGet()).toString();
    }

    private Boolean combineGranted(List<Permission> permissions) {
        return (Boolean)Observable.fromIterable(permissions).all(new Predicate<Permission>() {
            public boolean test(Permission permission) throws Exception {
                return permission.granted;
            }
        }).blockingGet();
    }

    private Boolean combineShouldShowRequestPermissionRationale(List<Permission> permissions) {
        return (Boolean)Observable.fromIterable(permissions).any(new Predicate<Permission>() {
            public boolean test(Permission permission) throws Exception {
                return permission.shouldShowRequestPermissionRationale;
            }
        }).blockingGet();
    }
}
