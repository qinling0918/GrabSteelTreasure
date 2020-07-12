//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tsinling.rxpermission3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build.VERSION;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;



import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RxPermissions {
    static final String TAG = RxPermissions.class.getSimpleName();
    static final Object TRIGGER = new Object();
    @VisibleForTesting
    RxPermissions.Lazy<RxPermissionsFragment> mRxPermissionsFragment;

    public RxPermissions(@NonNull FragmentActivity activity) {
        this.mRxPermissionsFragment = this.getLazySingleton(activity.getSupportFragmentManager());
    }

    public RxPermissions(@NonNull Fragment fragment) {
        this.mRxPermissionsFragment = this.getLazySingleton(fragment.getChildFragmentManager());
    }

    @NonNull
    private RxPermissions.Lazy<RxPermissionsFragment> getLazySingleton(@NonNull final FragmentManager fragmentManager) {
        return new RxPermissions.Lazy<RxPermissionsFragment>() {
            private RxPermissionsFragment rxPermissionsFragment;

            public synchronized RxPermissionsFragment get() {
                if (this.rxPermissionsFragment == null) {
                    this.rxPermissionsFragment = RxPermissions.this.getRxPermissionsFragment(fragmentManager);
                }

                return this.rxPermissionsFragment;
            }
        };
    }

    private RxPermissionsFragment getRxPermissionsFragment(@NonNull FragmentManager fragmentManager) {
        RxPermissionsFragment rxPermissionsFragment = this.findRxPermissionsFragment(fragmentManager);
        boolean isNewInstance = rxPermissionsFragment == null;
        if (isNewInstance) {
            rxPermissionsFragment = new RxPermissionsFragment();
            fragmentManager.beginTransaction().add(rxPermissionsFragment, TAG).commitNow();
        }

        return rxPermissionsFragment;
    }

    private RxPermissionsFragment findRxPermissionsFragment(@NonNull FragmentManager fragmentManager) {
        return (RxPermissionsFragment)fragmentManager.findFragmentByTag(TAG);
    }

    public void setLogging(boolean logging) {
        ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).setLogging(logging);
    }

    public <T> ObservableTransformer<T, Boolean> ensure(final String... permissions) {
        return new ObservableTransformer<T, Boolean>() {
            public ObservableSource<Boolean> apply(Observable<T> o) {
                return RxPermissions.this.request(o, permissions).buffer(permissions.length).flatMap(new Function<List<Permission>, ObservableSource<Boolean>>() {
                    public ObservableSource<Boolean> apply(List<Permission> permissionsx) {
                        if (permissionsx.isEmpty()) {
                            return Observable.empty();
                        } else {
                            Iterator var2 = permissionsx.iterator();

                            Permission p;
                            do {
                                if (!var2.hasNext()) {
                                    return Observable.just(true);
                                }

                                p = (Permission)var2.next();
                            } while(p.granted);

                            return Observable.just(false);
                        }
                    }
                });
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEach(final String... permissions) {
        return new ObservableTransformer<T, Permission>() {
            public ObservableSource<Permission> apply(Observable<T> o) {
                return RxPermissions.this.request(o, permissions);
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEachCombined(final String... permissions) {
        return new ObservableTransformer<T, Permission>() {
            public ObservableSource<Permission> apply(Observable<T> o) {
                return RxPermissions.this.request(o, permissions).buffer(permissions.length).flatMap(new Function<List<Permission>, ObservableSource<Permission>>() {
                    public ObservableSource<Permission> apply(List<Permission> permissionsx) {
                        return permissionsx.isEmpty() ? Observable.empty() : Observable.just(new Permission(permissionsx));
                    }
                });
            }
        };
    }

    public Observable<Boolean> request(String... permissions) {
        return Observable.just(TRIGGER).compose(this.ensure(permissions));
    }

    public Observable<Permission> requestEach(String... permissions) {
        return Observable.just(TRIGGER).compose(this.ensureEach(permissions));
    }

    public Observable<Permission> requestEachCombined(String... permissions) {
        return Observable.just(TRIGGER).compose(this.ensureEachCombined(permissions));
    }

    private Observable<Permission> request(Observable<?> trigger, final String... permissions) {
        if (permissions != null && permissions.length != 0) {
            return this.oneOf(trigger, this.pending(permissions)).flatMap(new Function<Object, Observable<Permission>>() {
                public Observable<Permission> apply(Object o) {
                    return RxPermissions.this.requestImplementation(permissions);
                }
            });
        } else {
            throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
        }
    }

    private Observable<?> pending(String... permissions) {
        String[] var2 = permissions;
        int var3 = permissions.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String p = var2[var4];
            if (!((RxPermissionsFragment)this.mRxPermissionsFragment.get()).containsByPermission(p)) {
                return Observable.empty();
            }
        }

        return Observable.just(TRIGGER);
    }

    private Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {
        return trigger == null ? Observable.just(TRIGGER) : Observable.merge(trigger, pending);
    }

    @TargetApi(23)
    private Observable<Permission> requestImplementation(String... permissions) {
        List<Observable<Permission>> list = new ArrayList(permissions.length);
        List<String> unrequestedPermissions = new ArrayList();
        String[] unrequestedPermissionsArray = permissions;
        int var5 = permissions.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String permission = unrequestedPermissionsArray[var6];
            ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).log("Requesting permission " + permission);
            if (this.isGranted(permission)) {
                list.add(Observable.just(new Permission(permission, true, false)));
            } else if (this.isRevoked(permission)) {
                list.add(Observable.just(new Permission(permission, false, false)));
            } else {
                PublishSubject<Permission> subject = ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).getSubjectByPermission(permission);
                if (subject == null) {
                    unrequestedPermissions.add(permission);
                    subject = PublishSubject.create();
                    ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).setSubjectForPermission(permission, subject);
                }

                list.add(subject);
            }
        }

        if (!unrequestedPermissions.isEmpty()) {
            unrequestedPermissionsArray = (String[])unrequestedPermissions.toArray(new String[unrequestedPermissions.size()]);
            this.requestPermissionsFromFragment(unrequestedPermissionsArray);
        }

        return Observable.concat(Observable.fromIterable(list));
    }

    public Observable<Boolean> shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        return !this.isMarshmallow() ? Observable.just(false) : Observable.just(this.shouldShowRequestPermissionRationaleImplementation(activity, permissions));
    }

    @TargetApi(23)
    private boolean shouldShowRequestPermissionRationaleImplementation(Activity activity, String... permissions) {
        String[] var3 = permissions;
        int var4 = permissions.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String p = var3[var5];
            if (!this.isGranted(p) && !activity.shouldShowRequestPermissionRationale(p)) {
                return false;
            }
        }

        return true;
    }

    @TargetApi(23)
    void requestPermissionsFromFragment(String[] permissions) {
        ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).log("requestPermissionsFromFragment " + TextUtils.join(", ", permissions));
        ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).requestPermissions(permissions);
    }

    public boolean isGranted(String permission) {
        return !this.isMarshmallow() || ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).isGranted(permission);
    }

    public boolean isRevoked(String permission) {
        return this.isMarshmallow() && ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).isRevoked(permission);
    }

    boolean isMarshmallow() {
        return VERSION.SDK_INT >= 23;
    }

    void onRequestPermissionsResult(String[] permissions, int[] grantResults) {
        ((RxPermissionsFragment)this.mRxPermissionsFragment.get()).onRequestPermissionsResult(permissions, grantResults, new boolean[permissions.length]);
    }

    @FunctionalInterface
    public interface Lazy<V> {
        V get();
    }
}
