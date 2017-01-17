package serial;


/**
 *
 * @author sx
 */
public class SerialPortException extends Exception
{

  SerialPortException ()
  {
    super();
  }


  public SerialPortException (String msg)
  {
    super(msg);
  }


  public SerialPortException (Throwable ex)
  {
    super(ex);
  }


  public SerialPortException (String message, Throwable cause)
  {
    super(message, cause);
  }


}
