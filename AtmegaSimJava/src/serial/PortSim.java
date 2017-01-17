package serial;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jni.App;
import logging.Logger;


/**
 *
 * @author sx
 */
public class PortSim implements Port
{
  private static final Logger LOG = Logger.getLogger(PortSim.class.getName());

  public static enum SIM_MODE
  {
    NORMAL
  }

  private SIM_MODE mode = SIM_MODE.NORMAL;
  private String portName;
  private final App app;
  private final String [] availablePorts = { "SIM-NORMAL" };
  private PipedOutputStream bufferOut;
  private final PipedInputStream bufferIn = new PipedInputStream();

  public PortSim (App app)
  {
    this.app = app;
  }

  @Override
  public boolean isOpened ()
  {
    return portName != null;
  }


  @Override
  public String[] getPortNames ()
  {
    return availablePorts;
  }


  @Override
  public String getPortName ()
  {
    return portName;
  }


  @Override
  public void openPort (String port) throws SerialPortException
  {
    if (this.portName != null)
      throw new SerialPortException("Port " + port + " already open");
    
    for (SIM_MODE m : SIM_MODE.values())
    {
      if (port.endsWith(m.name()))
      {
        try
        {
          bufferOut = new PipedOutputStream(bufferIn);
        
          app.setOut(new OutputStream() {

            @Override
            public void write (int b) throws IOException
            {
              synchronized (bufferIn)
              {
                bufferOut.write(b);
                bufferIn.notifyAll();
              }
            }
          
          });
          app_init();
          this.portName = port;
          mode = m;
          LOG.fine("PortSim: openPort(%s)", port);
          return;
        }
        catch (Exception ex)
        {
          throw new SerialPortException("open port " + port + " fails", ex);
        }
      }
    }
    throw new IllegalArgumentException("port " + port + " not supported");
  }

  
  @Override
  public void closePort () throws SerialPortException
  {
    if (this.portName == null)
      throw new SerialPortException("no port open");
    LOG.fine("PortSim: closePort()");
    this.portName = null;
    bufferOut = null;
    app.setOut(null);
  }


  @Override
  public void writeBytes (byte [] data) throws SerialPortException
  {
    if (!isOpened())
      throw new SerialPortException("Port not open, cannot write bytes");
    LOG.finer(data, "PortSim: writebytes(byte []) [mode=%s]", mode.name());
    try
    {
      for (byte b : data)
        app_uart_isr(b);
    }
    catch (Exception th)
    {
      LOG.warning(th);
      throw new SerialPortException(th);
    }
  }


  @Override
  public byte readByte () throws SerialPortException, InterruptedException
  {
    synchronized (bufferIn)
    {
      try
      {
        while (bufferIn.available() == 0 && portName != null )
        {
          bufferIn.wait();
        }
        if (bufferIn.available() != 0)
          return (byte) bufferIn.read();
      }
      catch (InterruptedException ex) {}
      catch (Exception ex)
      {
        throw new SerialPortException(ex);
      }
      
      throw new SerialPortException("port closed, no more bytes available");
    }
  }


  private void app_init () throws SerialPortException
  {
    try
    {
      app.init();
    }
    catch (Throwable th) // JNI Native functions cann throw Errors
    {
      throw new SerialPortException(th);
    }
  }
  
  private void app_uart_isr (byte b) throws SerialPortException
  {
    try
    {
      app.uart_isr(b);
    }
    catch (Throwable th) // JNI Native functions cann throw Errors
    {
      throw new SerialPortException(th);
    }
  }
  

}
