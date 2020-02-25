package fish.payara;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class PayaraBundle {

    private static Reference<ResourceBundle> bundleRef;
    private static final String BUNDLE = "fish.payara.PayaraBundle";

    private PayaraBundle() {
    }

    public static String message(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
            @NotNull Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(bundleRef);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            bundleRef = new SoftReference<>(bundle);
        }
        return bundle;
    }
}
