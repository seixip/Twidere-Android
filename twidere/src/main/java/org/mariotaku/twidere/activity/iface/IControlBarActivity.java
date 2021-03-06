package org.mariotaku.twidere.activity.iface;

/**
 * Created by mariotaku on 14/10/21.
 */
public interface IControlBarActivity {

    public void setControlBarOffset(float offset);

    public float getControlBarOffset();

    public int getControlBarHeight();

    public void moveControlBarBy(float delta);


    public void notifyControlBarOffsetChanged();

    public void registerControlBarOffsetListener(ControlBarOffsetListener listener);

    public void unregisterControlBarOffsetListener(ControlBarOffsetListener listener);

    public interface ControlBarOffsetListener {
        public void onControlBarOffsetChanged(IControlBarActivity activity, float offset);
    }
}
