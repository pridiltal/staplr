package pdftk.com.lowagie.text.pdf;

public class ICC_Profile {
    
    protected byte data[];
    protected int numComponents;
    
    protected ICC_Profile() {
    }
    
    public static ICC_Profile getInstance(byte data[], int numComponents) {
        ICC_Profile icc = new ICC_Profile();
        icc.data = data;
        icc.numComponents = numComponents;
        return icc;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public int getNumComponents() {
        return numComponents;
    }
}
