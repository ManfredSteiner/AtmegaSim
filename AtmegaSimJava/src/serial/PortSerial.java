package serial;

import java.util.LinkedList;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortList;
import logging.Logger;


/**
 * Several methods to communicate with the serial Port
 *
 * @author sx
 */
public class PortSerial implements Port, SerialPortEventListener
{
  private static final Logger LOG = Logger.getLogger(PortSerial.class.getName());

  private SerialPort serialPort;
  private final LinkedList<PortSerialChunk> receivedChunks = new LinkedList<>();


  public PortSerial ()
  {
  }


  /**
   * @return the String array of available ports
   */
  @Override
  public String[] getPortNames ()
  {
    return SerialPortList.getPortNames();
  }


  /**
   * @return the current port
   */
  @Override
  public String getPortName ()
  {
    return serialPort.getPortName();
  }

  
  public boolean setParams (int baudRate, int dataBits, int stopBits, int parity)
    throws jssc.SerialPortException 
  {
    return serialPort.setParams(baudRate, dataBits, stopBits, parity);
  }


  public boolean setParams (int baudRate, int dataBits, int stopBits, int parity, boolean setRTS, boolean setDTR)
    throws jssc.SerialPortException 
  {
    return serialPort.setParams(baudRate, dataBits, stopBits, parity, setRTS, setDTR);
  }


  @Override
  public void openPort (String port) throws SerialPortException
  {
    if (serialPort != null)
      throw new SerialPortException("serial port already open");
    try
    {
      serialPort = new SerialPort(port);
      LOG.fine("Open serial port %s (57600/8N1)", serialPort.getPortName());
      if (!serialPort.openPort())
      {
        throw new SerialPortException("open Port failed");
      }

      serialPort.setParams(SerialPort.BAUDRATE_57600,
                           SerialPort.DATABITS_8,
                           SerialPort.STOPBITS_1,
                           SerialPort.PARITY_NONE);
      serialPort.addEventListener(this);
    }
    catch (Exception ex)
    {
      throw new SerialPortException(ex);
    }
  }


  @Override
  public void closePort () throws SerialPortException
  {
    try
    {
      LOG.fine("close serial port %s", serialPort.getPortName());
      if (!serialPort.closePort())
      {
        throw new SerialPortException("closing Port failed");
      }

      serialPort = null;
    }
    catch (SerialPortException ex)
    {
      throw new SerialPortException("closing Port failed");
    }
    catch (Exception ex)
    {
      throw new SerialPortException(ex);
    }
  }


  @Override
  public byte readByte () throws SerialPortException, InterruptedException
  {
    synchronized (receivedChunks)
    {
      try
      {
        while (receivedChunks.isEmpty())
        {
          receivedChunks.wait();
        }

        final PortSerialChunk ch = receivedChunks.getFirst();

        final byte rv = ch.next();
        if (!ch.isByteAvailable())
        {  
          receivedChunks.removeFirst();
        }
        return rv;
      }
      catch (InterruptedException ex)
      {
        throw ex;
      }
      catch (Exception ex)
      {
        throw new SerialPortException(ex);
      }

    }
  }

  //private static boolean block = false;
  @Override
  public synchronized void writeBytes (byte[] data) throws SerialPortException
  {
    LOG.finer(data, "PortSerial: writebytes(byte [])");
    try
    {
      serialPort.writeBytes(data);
      LOG.finest("bytes written");
    }
    catch (Exception ex)
    {
      throw new SerialPortException(ex);
    }

  }


  @Override
  public void serialEvent (SerialPortEvent serialPortEvent)
  {
    if (serialPortEvent.getEventType() == SerialPortEvent.RXCHAR)
    {
      try
      {
        byte[] ba = serialPort.readBytes();
        LOG.finer("PortSerial: %s bytes received", ba.length);
        synchronized (receivedChunks)
        {
          receivedChunks.add(new PortSerialChunk(ba));
          receivedChunks.notifyAll();
        }
      }
      catch (Exception ex)
      {
        LOG.warning(ex);
      }

    }
  }


  @Override
  public boolean isOpened ()
  {
    return serialPort != null && serialPort.isOpened();
  }

  
  private class PortSerialChunk 
  {
    private final byte[] data;
    private int nextIndex = 0;

    public PortSerialChunk (byte[] data)
    {
      if (data == null)
      {
        throw new NullPointerException();
      }

      this.data = data;
    }

    public byte next ()
    {
      return data[nextIndex++];
    }

    public boolean isByteAvailable ()
    {
      return data == null ? (nextIndex == 0) : (nextIndex < data.length);
    }
  }

}
