
package org.holoeverywhere;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.holoeverywhere.SystemServiceManager.SystemServiceCreator;
import org.holoeverywhere.SystemServiceManager.SystemServiceCreator.SystemService;
import org.holoeverywhere.internal.DialogTitle;
import org.holoeverywhere.internal.NumberPickerEditText;
import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.internal.view.menu.ContextMenuDecorView;
import com.actionbarsherlock.internal.view.menu.ContextMenuListener;
import com.actionbarsherlock.internal.view.menu.ExpandedMenuView;
import com.actionbarsherlock.internal.view.menu.HoloListMenuItemView;
import com.actionbarsherlock.internal.widget.ActionBarContainer;
import com.actionbarsherlock.internal.widget.ActionBarView;

public class LayoutInflater extends android.view.LayoutInflater implements
        Cloneable, android.view.LayoutInflater.Factory {
    private final class HoloFactoryMerger extends ArrayList<Factory> implements
            Factory {
        private static final long serialVersionUID = -851134244408815411L;

        @Override
        public View onCreateView(String name, Context context,
                AttributeSet attrs) {
            for (Factory factory : this) {
                try {
                    View view = factory.onCreateView(name, context, attrs);
                    if (view != null) {
                        return view;
                    }
                } catch (Exception e) {
                }
            }
            return null;
        }
    }

    @SystemService(Context.LAYOUT_INFLATER_SERVICE)
    public static class LayoutInflaterCreator implements
            SystemServiceCreator<LayoutInflater> {
        @Override
        public LayoutInflater createService(Context context) {
            return LayoutInflater.from(context);
        }
    }

    public static interface OnInitInflaterListener {
        public void onInitInflater(LayoutInflater inflater);
    }

    private static final Map<Context, WeakReference<LayoutInflater>> INSTANCES_MAP = new WeakHashMap<Context, WeakReference<LayoutInflater>>();
    private static OnInitInflaterListener listener;
    private static final List<String> PACKAGES_LIST = new ArrayList<String>();
    private static final Map<String, String> VIEWS_MAP = new HashMap<String, String>();

    static {
        registerPackage(HoloEverywhere.PACKAGE + ".widget");
        registerPackage("android.support.v4.view");
        registerPackage("android.widget");
        registerPackage("android.view");
        registerPackage("android.webkit");
        remapInternal(ActionBarView.class, HoloListMenuItemView.class,
                ExpandedMenuView.class, ActionBarContainer.class, DialogTitle.class,
                NumberPickerEditText.class);
    }

    public static void clearInstances() {
        LayoutInflater.INSTANCES_MAP.clear();
    }

    public static LayoutInflater from(android.view.LayoutInflater inflater) {
        if (inflater instanceof LayoutInflater) {
            return (LayoutInflater) inflater;
        }
        return new LayoutInflater(inflater, inflater.getContext());
    }

    public static LayoutInflater from(Context context) {
        LayoutInflater inflater = null;
        WeakReference<LayoutInflater> reference = INSTANCES_MAP.get(context);
        if (reference != null) {
            inflater = reference.get();
        }
        if (inflater == null) {
            inflater = new LayoutInflater(context);
            reference = new WeakReference<LayoutInflater>(inflater);
            INSTANCES_MAP.put(context, reference);
        }
        return inflater;
    }

    public static LayoutInflater from(View view) {
        return LayoutInflater.from(view.getContext());
    }

    public static View inflate(Context context, int resource) {
        return LayoutInflater.from(context).inflate(resource, null);
    }

    public static View inflate(Context context, int resource, ViewGroup root) {
        return LayoutInflater.from(context).inflate(resource, root);
    }

    public static View inflate(Context context, int resource, ViewGroup root,
            boolean attachToRoot) {
        return LayoutInflater.from(context).inflate(resource, root,
                attachToRoot);
    }

    public static View inflate(View view, int resource) {
        return LayoutInflater.from(view).inflate(resource, null);
    }

    public static View inflate(View view, int resource, ViewGroup root) {
        return LayoutInflater.from(view).inflate(resource, root);
    }

    public static View inflate(View view, int resource, ViewGroup root,
            boolean attachToRoot) {
        return LayoutInflater.from(view).inflate(resource, root, attachToRoot);
    }

    public static void onDestroy(Context context) {
        LayoutInflater.INSTANCES_MAP.remove(context);
    }

    public static void registerPackage(String packageName) {
        packageName = Package.getPackage(packageName).getName();
        if (!PACKAGES_LIST.contains(packageName)) {
            PACKAGES_LIST.add(packageName);
        }
    }

    public static void remap(Class<? extends View>... classes) {
        for (Class<? extends View> clazz : classes) {
            remap(clazz);
        }
    }

    public static void remap(Class<? extends View> clazz) {
        if (clazz != null) {
            LayoutInflater.VIEWS_MAP
                    .put(clazz.getSimpleName(), clazz.getName());
        }
    }

    @Deprecated
    public static void remap(String prefix, String... classess) {
        for (String clazz : classess) {
            LayoutInflater.VIEWS_MAP.put(clazz, prefix + "." + clazz);
        }
    }

    public static void remapHard(String from, String to) {
        LayoutInflater.VIEWS_MAP.put(from, to);
    }

    private static void remapInternal(Class<?>... classess) {
        for (Class<?> clazz : classess) {
            remapHard("Internal." + clazz.getSimpleName(), clazz.getName());
        }
    }

    public static void setOnInitInflaterListener(OnInitInflaterListener listener) {
        LayoutInflater.listener = listener;
    }

    private final HoloFactoryMerger factoryMerger = new HoloFactoryMerger();

    protected LayoutInflater(android.view.LayoutInflater original,
            Context newContext) {
        super(original, newContext);
        init();
    }

    protected LayoutInflater(Context context) {
        super(context);
        init();
    }

    public void addFactory(Factory factory) {
        checkFactoryOnNull(factory);
        factoryMerger.add(factory);
    }

    public void addFactory(Factory factory, int index) {
        checkFactoryOnNull(factory);
        factoryMerger.add(index, factory);
    }

    private void checkFactoryOnNull(Factory factory) {
        if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        }
    }

    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return new LayoutInflater(this, newContext);
    }

    public View inflate(int resource) {
        return inflate(resource, null);
    }

    @Override
    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, root != null);
    }

    @Override
    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        View view = super.inflate(resource, root, attachToRoot);
        if (view != null) {
            view = FontLoader.applyDefaultStyles(view);
        }
        return view;
    }

    @Override
    public View inflate(XmlPullParser parser, ViewGroup root) {
        return inflate(parser, root, root != null);
    }

    @Override
    public View inflate(XmlPullParser parser, ViewGroup root,
            boolean attachToRoot) {
        View view = super.inflate(parser, root, attachToRoot);
        if (view != null) {
            view = FontLoader.applyDefaultStyles(view);
        }
        return view;
    }

    private void init() {
        super.setFactory(factoryMerger);
        factoryMerger.add(this);
        if (LayoutInflater.listener != null) {
            LayoutInflater.listener.onInitInflater(this);
        }
    }

    public ContextMenuDecorView makeDecorView(int layout, ContextMenuListener listener) {
        return ContextMenuDecorView.inflateDecorView(this, layout, listener);
    }

    @Override
    protected View onCreateView(String name, AttributeSet attrs)
            throws ClassNotFoundException {
        String newName = LayoutInflater.VIEWS_MAP.get(name.intern());
        View view;
        if (newName != null) {
            view = tryCreateView(newName, null, attrs);
            if (view != null) {
                return prepareView(view);
            }
        }
        if (name.indexOf('.') > 0) {
            view = tryCreateView(name, null, attrs);
            if (view != null) {
                return prepareView(view);
            }
        }
        for (int i = 0; i < PACKAGES_LIST.size(); i++) {
            view = tryCreateView(name, PACKAGES_LIST.get(i) + ".", attrs);
            if (view != null) {
                return prepareView(view);
            }
        }
        throw new ClassNotFoundException("Could not find class: " + name);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        try {
            return onCreateView(name, attrs);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("NewApi")
    private View prepareView(View view) {
        if (HoloEverywhere.DISABLE_OVERSCROLL_EFFECT && VERSION.SDK_INT >= 9) {
            view.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        return view;
    }

    @Override
    public void setFactory(Factory factory) {
        addFactory(factory, 0);
    }

    protected View tryCreateView(String name, String prefix, AttributeSet attrs) {
        String newName = prefix == null ? "" : prefix;
        newName += name;
        try {
            if (Class.forName(newName) != null) {
                return createView(newName, null, attrs);
            }
        } catch (ClassNotFoundException e) {
        }
        return null;
    }
}
