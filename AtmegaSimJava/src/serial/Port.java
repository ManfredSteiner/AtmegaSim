package serial;

/**
 *
 * @author Levin
 */
public interface Port
{
    void openPort (String port) throws SerialPortException;
    void closePort() throws SerialPortException;
    boolean isOpened ();
    void writeBytes(byte[] s) throws SerialPortException;
    byte readByte() throws SerialPortException, InterruptedException;
    String [] getPortNames();
    String getPortName();
}
