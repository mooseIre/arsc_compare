package com.android.systemui.statusbar.notification;

import android.util.FloatProperty;
import android.util.Property;
import android.view.View;
import com.android.systemui.C0015R$id;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AnimatableProperty {
    public static final AnimatableProperty X = from(View.X, C0015R$id.x_animator_tag, C0015R$id.x_animator_tag_start_value, C0015R$id.x_animator_tag_end_value);
    public static final AnimatableProperty Y = from(View.Y, C0015R$id.y_animator_tag, C0015R$id.y_animator_tag_start_value, C0015R$id.y_animator_tag_end_value);

    public abstract int getAnimationEndTag();

    public abstract int getAnimationStartTag();

    public abstract int getAnimatorTag();

    public abstract Property getProperty();

    static {
        from(new FloatProperty<View>("ViewAbsoluteX") {
            /* class com.android.systemui.statusbar.notification.AnimatableProperty.AnonymousClass1 */

            public void setValue(View view, float f) {
                view.setTag(C0015R$id.absolute_x_current_value, Float.valueOf(f));
                View.X.set(view, Float.valueOf(f));
            }

            public Float get(View view) {
                Object tag = view.getTag(C0015R$id.absolute_x_current_value);
                if (tag instanceof Float) {
                    return (Float) tag;
                }
                return (Float) View.X.get(view);
            }
        }, C0015R$id.absolute_x_animator_tag, C0015R$id.absolute_x_animator_start_tag, C0015R$id.absolute_x_animator_end_tag);
        from(new FloatProperty<View>("ViewAbsoluteY") {
            /* class com.android.systemui.statusbar.notification.AnimatableProperty.AnonymousClass2 */

            public void setValue(View view, float f) {
                view.setTag(C0015R$id.absolute_y_current_value, Float.valueOf(f));
                View.Y.set(view, Float.valueOf(f));
            }

            public Float get(View view) {
                Object tag = view.getTag(C0015R$id.absolute_y_current_value);
                if (tag instanceof Float) {
                    return (Float) tag;
                }
                return (Float) View.Y.get(view);
            }
        }, C0015R$id.absolute_y_animator_tag, C0015R$id.absolute_y_animator_start_tag, C0015R$id.absolute_y_animator_end_tag);
        from(new FloatProperty<View>("ViewWidth") {
            /* class com.android.systemui.statusbar.notification.AnimatableProperty.AnonymousClass3 */

            public void setValue(View view, float f) {
                view.setTag(C0015R$id.view_width_current_value, Float.valueOf(f));
                view.setRight((int) (((float) view.getLeft()) + f));
            }

            public Float get(View view) {
                Object tag = view.getTag(C0015R$id.view_width_current_value);
                if (tag instanceof Float) {
                    return (Float) tag;
                }
                return Float.valueOf((float) view.getWidth());
            }
        }, C0015R$id.view_width_animator_tag, C0015R$id.view_width_animator_start_tag, C0015R$id.view_width_animator_end_tag);
        from(new FloatProperty<View>("ViewHeight") {
            /* class com.android.systemui.statusbar.notification.AnimatableProperty.AnonymousClass4 */

            public void setValue(View view, float f) {
                view.setTag(C0015R$id.view_height_current_value, Float.valueOf(f));
                view.setBottom((int) (((float) view.getTop()) + f));
            }

            public Float get(View view) {
                Object tag = view.getTag(C0015R$id.view_height_current_value);
                if (tag instanceof Float) {
                    return (Float) tag;
                }
                return Float.valueOf((float) view.getHeight());
            }
        }, C0015R$id.view_height_animator_tag, C0015R$id.view_height_animator_start_tag, C0015R$id.view_height_animator_end_tag);
    }

    public static <T extends View> AnimatableProperty from(String str, final BiConsumer<T, Float> biConsumer, final Function<T, Float> function, final int i, final int i2, final int i3) {
        final AnonymousClass5 r0 = new FloatProperty<T>(str) {
            /* class com.android.systemui.statusbar.notification.AnimatableProperty.AnonymousClass5 */

            public Float get(T t) {
                return (Float) function.apply(t);
            }

            public void setValue(T t, float f) {
                biConsumer.accept(t, Float.valueOf(f));
            }
        };
        return new AnimatableProperty() {
            /* class com.android.systemui.statusbar.notification.AnimatableProperty.AnonymousClass6 */

            @Override // com.android.systemui.statusbar.notification.AnimatableProperty
            public int getAnimationStartTag() {
                return i2;
            }

            @Override // com.android.systemui.statusbar.notification.AnimatableProperty
            public int getAnimationEndTag() {
                return i3;
            }

            @Override // com.android.systemui.statusbar.notification.AnimatableProperty
            public int getAnimatorTag() {
                return i;
            }

            @Override // com.android.systemui.statusbar.notification.AnimatableProperty
            public Property getProperty() {
                return r0;
            }
        };
    }

    public static <T extends View> AnimatableProperty from(final Property<T, Float> property, final int i, final int i2, final int i3) {
        return new AnimatableProperty() {
            /* class com.android.systemui.statusbar.notification.AnimatableProperty.AnonymousClass7 */

            @Override // com.android.systemui.statusbar.notification.AnimatableProperty
            public int getAnimationStartTag() {
                return i2;
            }

            @Override // com.android.systemui.statusbar.notification.AnimatableProperty
            public int getAnimationEndTag() {
                return i3;
            }

            @Override // com.android.systemui.statusbar.notification.AnimatableProperty
            public int getAnimatorTag() {
                return i;
            }

            @Override // com.android.systemui.statusbar.notification.AnimatableProperty
            public Property getProperty() {
                return property;
            }
        };
    }
}
