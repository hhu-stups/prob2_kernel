package de.prob.cli;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

public class ProBInstanceProviderTest {

	@Test
	public void testExtractCliInformation() {
		ProBInstanceProvider factory = CliTestCommon.getInjector().getInstance(ProBInstanceProvider.class);

		String text = "No file to process\nStarting Socket Server\n"
				+ "Application Path: /Users/bendisposto/.prob\nPort: 61013\n"
				+ "probcli revision: $Rev$\nuser interrupt reference id: 57124\n"
				+ "-- starting command loop --";

		BufferedReader reader = new BufferedReader(new StringReader(text));

		ProBInstanceProvider.CliInformation info = factory.extractCliInformation(reader);

		Assert.assertNotNull(info);
		Assert.assertEquals(61013, info.getPort());
		Assert.assertEquals(57124, info.getUserInterruptReference());
	}
}
