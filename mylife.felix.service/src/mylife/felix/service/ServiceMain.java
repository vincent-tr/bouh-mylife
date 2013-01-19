package mylife.felix.service;

import java.util.Properties;

import org.apache.commons.daemon.*;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.*;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

public class ServiceMain implements Daemon {

	// repris de http://svn.apache.org/viewvc/felix/trunk/main/src/main/java/org/apache/felix/main/Main.java
	
	private Framework m_fwk = null;
	
	@Override
	public void init(DaemonContext ctx) throws DaemonInitException, Exception {
		
		final String[] args = ctx.getArguments();
		
        // Look for bundle directory and/or cache directory.
        // We support at most one argument, which is the bundle
        // cache directory.
        String bundleDir = null;
        String cacheDir = null;
        boolean expectBundleDir = false;
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals(Main.BUNDLE_DIR_SWITCH))
            {
                expectBundleDir = true;
            }
            else if (expectBundleDir)
            {
                bundleDir = args[i];
                expectBundleDir = false;
            }
            else
            {
                cacheDir = args[i];
            }
        }

        if ((args.length > 3) || (expectBundleDir && bundleDir == null))
            throw new DaemonInitException("Usage: [-b <bundle-deploy-dir>] [<bundle-cache-dir>]");

        // Load system properties.
        Main.loadSystemProperties();

        // Read configuration properties.
        Properties configProps = Main.loadConfigProperties();
        // If no configuration properties were found, then create
        // an empty properties object.
        if (configProps == null)
        {
            System.err.println("No " + Main.CONFIG_PROPERTIES_FILE_VALUE + " found.");
            configProps = new Properties();
        }

        // Copy framework properties from the system properties.
        Main.copySystemProperties(configProps);

        // If there is a passed in bundle auto-deploy directory, then
        // that overwrites anything in the config file.
        if (bundleDir != null)
        {
            configProps.setProperty(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, bundleDir);
        }

        // If there is a passed in bundle cache directory, then
        // that overwrites anything in the config file.
        if (cacheDir != null)
        {
            configProps.setProperty(Constants.FRAMEWORK_STORAGE, cacheDir);
        }
/*
        // If enabled, register a shutdown hook to make sure the framework is
        // cleanly shutdown when the VM exits.
        String enableHook = configProps.getProperty(Main.SHUTDOWN_HOOK_PROP);
        if ((enableHook == null) || !enableHook.equalsIgnoreCase("false"))
        {
            Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
                public void run()
                {
                    try
                    {
                        if (m_fwk != null)
                        {
                            m_fwk.stop();
                            m_fwk.waitForStop(0);
                        }
                    }
                    catch (Exception ex)
                    {
                        System.err.println("Error stopping framework: " + ex);
                    }
                }
            });
        }
*/
        // Create an instance of the framework.
        FrameworkFactory factory = new FrameworkFactory();
        m_fwk = factory.newFramework(configProps);
        // Initialize the framework, but don't start it yet.
        m_fwk.init();
        // Use the system bundle context to process the auto-deploy
        // and auto-install/auto-start properties.
        AutoProcessor.process(configProps, m_fwk.getBundleContext());
	}

	@Override
	public void start() throws Exception {
		m_fwk.start();
	}

	@Override
	public void stop() throws Exception {
		m_fwk.stop();
		m_fwk.waitForStop(0);
	}

	@Override
	public void destroy() {
		m_fwk = null;
	}

}
