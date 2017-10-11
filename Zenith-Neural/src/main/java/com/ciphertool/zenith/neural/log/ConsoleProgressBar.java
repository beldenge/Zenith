package com.ciphertool.zenith.neural.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleProgressBar {
	private static Logger	log			= LoggerFactory.getLogger(ConsoleProgressBar.class);

	private int				progress	= 0;

	private int				barLength	= 100;

	private boolean			complete	= false;

	public void tick(double current, double total) {
		if (complete) {
			return;
		}

		double progressPercent = current / total;

		int ticksToPrint = (int) (progressPercent * (double) barLength);

		if (ticksToPrint > progress) {
			this.progress = ticksToPrint;

			if (log.isDebugEnabled()) {
				System.out.print("\r" + print(ticksToPrint, (int) (progressPercent * 100.0)));
			}
		}
	}

	private String print(int ticks, int progressPercent) {
		if (ticks > barLength) {
			throw new IllegalArgumentException("The number of ticks to print (" + ticks
					+ ") exceeds the progress bar length of " + barLength + ".");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("[");

		for (int i = 0; i < ticks; i++) {
			sb.append("-");
		}

		for (int i = ticks; i < barLength; i++) {
			sb.append(" ");
		}

		sb.append("]");

		sb.append(" " + progressPercent + "%");

		if (ticks == barLength) {
			complete = true;

			sb.append("\n");
		}
		
		return sb.toString();
	}
}
