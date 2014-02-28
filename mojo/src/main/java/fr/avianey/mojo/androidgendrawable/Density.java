package fr.avianey.mojo.androidgendrawable;


public enum Density {
    
    ldpi(120), mdpi(160), hdpi(240), xhdpi(320), tvdpi(213), xxhdpi(480), xxxhdpi(640);
    
    private final int dpi;

    private Density(int dpi) {
        this.dpi = dpi;
    }

    public double ratio(Density target) {
        return (double) target.dpi / (double) this.dpi;
    }

	public int getDpi() {
		return dpi;
	}
}
