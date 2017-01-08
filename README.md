# AtmegaSim

The goal of this project is to join a Java program with a Atmega microcontroller C program, in order
to improve debugging capabilities, for example if you have to implement a communicaton protocol stack 
between a Java program on the one side and a microcontroller on the other side.

This project is created and tested with Netbeans 8.2 under Linux. The usage of other 
IDEs, versions or operating systems may cause problems which were not checked up to now.

## Build and run

Follow the following procedure to build and run this project...

1. `git clone <repository>`
2. `cd <repository>/AtmegaSimJava`
3. `make`  
     Now the Java project is build and the tool *javah* creates the file 
     **AtmegaSimSharedLib/src/jni_App.h** which is needed by the shared library project.
4. `cd ../AtmegaSimSharedLib`
5. `make`  
     Now the native shared library is build and saved in 
     **AtmegaSimSharedLib/dist/Debug_linux_Gnu-Linux/libAtmegaSimSharedLib.so**
6.  `cd ..`
7.  Start the application with... 
    `java -jar dist/AtmegaSimJava`


## Java Project

See subproject [AtmegaSimJava](AtmegaSimJava)

The Java Native Interface (JNI) is used to bind the native shared library 
[libAtmegaSimSharedLib.so](/libAtmegaSimSharedLib) with the Java VM. 
The library file itself is build in the subproject **AtmegaSimSharedLib** and 
only a symbolic link is used to make the library available for Javas *System.load(..)*.

So you need to build AtmegaSimSharedLib first, before you can start the Java application.

## Native Shared Library C Project

See subproject [AtmegaSimSharedLib](AtmegaSimSharedLib).

In the directory [src](AtmegaSimSharedLib/src) are the the sources for the shared library. 
The files [global.h](ArduinoNano/src/global.h), [app.h](ArduinoNano/src/app.h) and [app.c](ArduinoNano/src/app.c) 
are only sy,bolic links to the original microcontroller project [ArduinoNano](ArduinoNano).

The project is compiled and linked with the GNU **-g** option, the support debugging information inside the 
shared library.

In Netbeans project properties (category Build/C++ Compile) the include directories are set to
[/usr/lib/jvm/java-8-oracle/include/linux](file:///usr/lib/jvm/java-8-oracle/include/linux) and
[/usr/lib/jvm/java-8-oracle/include](file:///usr/lib/jvm/java-8-oracle/include).
This is needed because the file [AtmegaSimSharedLib/src/jni_App.c](AtmegaSimSharedLib/src/jni_App.c) includes
the file [jni.h](file:///usr/lib/jvm/java-8-oracle/include/jni.h), which is located in the installation directory of your java development kit.
Maybe you have to adjust these values to your system.

--------------------------------------------------------

## Debugging with gdb

At first start the Java GUI application and the afterward the GNU-Debugger **gdb**

1. `cd <project-dir>/AtmegaSimJava`
2. `java -Djava.library.path=<project-dir>/AtmegaSimJava -jar dist/AtmegaSimJava.jar &`
3. `sudo -i`
4. `gdb`
```

Now you have to attach the GNU-Debugger to the running Java program.
For the attach command you need the process ID (PID visible in title bar).

After attachment you can pause the program with pressing CTRL+C. 
Now you can use the following commands to debug your simulated microcontroller program.

```
attach <pid>
detach

info source
info functions
info breakpoints
info proc map

list <function>
break <line>

frame
continue
next
info args
info locals
info
```
