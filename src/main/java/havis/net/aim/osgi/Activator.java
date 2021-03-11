package havis.net.aim.osgi;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import havis.device.io.IODevice;
import havis.device.rf.RFDevice;
import havis.net.aim.device.rf.AimRfidReaderDevice;
import havis.net.aim.opcua.AimDataProvider;
import havis.net.aim.opcua.ConfigurationManager;
import havis.net.aim.opcua.DeserializerException;
import havis.net.aim.rest.RESTApplication;
import havis.net.aim.rest.SecurityConfiguration;
import havis.opcua.message.MessageHandler;
import havis.opcua.message.common.MessageHandlerCommon;

public class Activator implements BundleActivator {

	private Logger log = Logger.getLogger(Activator.class.getName());

	private ServiceTracker<RFDevice, RFDevice> trackerRf;
	private ServiceTracker<IODevice, IODevice> trackerIo;
	private RFDevice rfDevice;
	private IODevice ioDevice;
	private ServiceRegistration<Application> restService;
	MessageHandler handler;

	@Override
	public void start(BundleContext context) throws Exception {
		long now = new Date().getTime();
		final Activator parent = this;

		trackerRf = new ServiceTracker<RFDevice, RFDevice>(context, RFDevice.class, null) {
			@Override
			public RFDevice addingService(ServiceReference<RFDevice> reference) {
				rfDevice = super.addingService(reference);
				// if IO device is available
				if (ioDevice != null) {
					// open message handler and rest service
					parent.open(context, rfDevice, ioDevice);
				}
				return rfDevice;
			}

			@Override
			public void removedService(ServiceReference<RFDevice> reference, RFDevice service) {
				// close message handler and rest service
				parent.close();
				rfDevice = null;
				super.removedService(reference, service);
			}
		};
		trackerRf.open();

		trackerIo = new ServiceTracker<IODevice, IODevice>(context, IODevice.class, null) {
			@Override
			public IODevice addingService(ServiceReference<IODevice> reference) {
				ioDevice = super.addingService(reference);
				// if RF device is available
				if (rfDevice != null) {
					// open message handler and rest service
					parent.open(context, rfDevice, ioDevice);
				}
				return ioDevice;
			}

			@Override
			public void removedService(ServiceReference<IODevice> reference, IODevice service) {
				// close message handler and rest service
				parent.close();
				ioDevice = null;
				super.removedService(reference, service);
			}
		};
		trackerIo.open();

		log.info("Bundle start took " + (new Date().getTime() - now) + "ms");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (trackerIo != null) {
			trackerIo.close();
			trackerIo = null;
		}

		if (trackerRf != null) {
			trackerRf.close();
			trackerRf = null;
		}

		// close message handler and rest service
		close();

		rfDevice = null;
		ioDevice = null;
	}

	private void open(BundleContext context, RFDevice rfDevice, IODevice ioDevice) {
		handler = new MessageHandlerCommon();
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.FINE, "Opening message handler");
		}

		ConfigurationManager configurationHelper = new ConfigurationManager();
		SecurityConfiguration security = new SecurityConfiguration();
		restService = context.registerService(Application.class, new RESTApplication(configurationHelper, security),
				null);
		try {
			handler.open(
					new AimDataProvider(new AimRfidReaderDevice(rfDevice, ioDevice, configurationHelper), handler));
		} catch (DeserializerException e) {
			log.log(Level.SEVERE, "Cannot open message handler", e);
		}
	}

	private void close() {
		if (handler != null) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "Closing message handler");
			}
			handler.close();
			handler = null;
		}

		if (restService != null) {
			restService.unregister();
			restService = null;
		}
	}
}