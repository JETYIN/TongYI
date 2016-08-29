package android.os;

interface IHardwareService {
	boolean getFlashlightEnabled();

	void setFlashlightEnabled(boolean on);
}