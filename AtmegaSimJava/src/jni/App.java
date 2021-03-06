package jni;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import logging.Logger;


/**
 *
 * @author sx@htl-kaindorf.at
 */
public class App
{
  static {
    //System.loadLibrary("libnativeappsim");
    System.loadLibrary("AtmegaSimSharedLib");
    File f = new File(System.getProperty("user.dir") + "/../AtmegaSimSharedLib/");
    String path = null;
    try { path = f.getCanonicalPath() + "/"; } catch (Exception ex) {}
    NATIVE_PATH = path;
  }
  
  private static final Logger LOG = Logger.getLogger(App.class.getName());
  private static final String NATIVE_PATH;
  
  // ****************************************************************************************
  
  private OutputStream out;
  private final OutputStream log;  
  
  public native String nativeVersion();
  public native void init ();
  public native void main ();
  public native void task_1ms ();
  public native void task_2ms ();
  public native void task_4ms ();
  public native void task_8ms ();
  public native void task_16ms ();
  public native void task_32ms ();
  public native void task_64ms ();
  public native void task_128ms ();
  public native void timer0_ovf ();
  public native void timer1_ovf ();
  public native void timer2_ovf ();
  public native void uart_isr (byte b);
  public native void sys_500us_isr ();


  public App ()
  {
    this(null);
  }
  
  public App (OutputStream out)
  {
    setOut(out);

    log = new OutputStream()
    {
      private ByteArrayOutputStream baos;
      
      @Override
      public void write (int b) throws IOException
      {
        if (baos == null)
          baos = new ByteArrayOutputStream(256);
        if (b == 0) 
        {
          String msg = new String(baos.toByteArray());
          String location = null;
          baos.reset();
          if (!msg.startsWith("/"))
          {
            int i = msg.indexOf(')');
            if (i > 0)
            {
              location = NATIVE_PATH + msg.substring(0, i+1);
              msg = "NATIVE -> " + msg.substring(i+2);
            }
          }
          LOG.info(msg);
          if (location != null)
            System.out.println(location);
        }
        else
        {
          baos.write(b);
        }
      }
    };
    
  }
  
  
  public void setOut (OutputStream out)
  {
    if (out == null)
      this.out = new DefaultOutputStream();
    else
      this.out = out;
  }
  
  
  public String version ()
  {
    return "1.0";
  }
  
  public static void test () 
  {
    App app = new App();
    System.out.println("Version: " + app.version() + "  Native: " + app.nativeVersion());
    app.init();
    app.main();
    app.task_1ms();
    app.task_2ms();
    app.task_4ms();
    app.task_8ms();
    app.task_16ms();
    app.task_32ms();
    app.task_64ms();
    app.task_128ms();
    app.timer0_ovf();
    app.timer1_ovf();
    app.timer2_ovf();
    app.uart_isr((byte)0xab);
  }

  
  private class DefaultOutputStream extends OutputStream
  {
    @Override
    public void write (int b) throws IOException
    {
      LOG.info(" ---> From Simulation Out: 0x%02x", b);
    }
  }


  
}
