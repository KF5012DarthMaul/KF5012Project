package kf5012darthmaulapplication;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.swing.JPanel;

import temporal.ChartableEvent;
import temporal.Event;
import temporal.TemporalMap;
import temporal.Timeline;

public class TimelinePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/* Retrievable
	 * ---------- */
	
	private final Timeline<Integer, ChartableEvent> timeline;
	private LocalDateTime start;
	private LocalDateTime end;

	private final int width;
	private final int height;
	
	/* Internal
	 * ---------- */
	
	// Graphics
	private final Stroke axisStroke = new BasicStroke(3f);
	private final Stroke timelineStroke = new BasicStroke(5f);
	
	// Overall measures
	private final int margin;
	private final int plotWidth;
	private final int plotHeight;
	private int xAxisHeight;
	private int timelineHeight;
	
	// Font measures
	private FontMetrics metrics;
	
	// Axis tick measures
	private final int tickLength;
	private final int hangingTickLength;
	private Duration tickInterval;
	private Duration hangingStartTickInterval;
	private Duration hangingEndTickInterval;
	private DateTimeFormatter tickLabelFormatter;
	private int numTicks;
	private boolean hangingStartTick;
	private boolean hangingEndTick;
	int tickIntervalPx;
	
	// Axis labels
	private String[] xAxis;

	public TimelinePanel(
			Timeline<Integer, ChartableEvent> timeline,
			int width, int height
	) {
		this.timeline = timeline;
		
		this.width = width;
		this.height = height;
		this.setPreferredSize(new Dimension(width, height));

		this.margin = 50;
		this.plotWidth = width - margin * 2;
		this.plotHeight = height - margin * 2;

		this.tickLength = 20;
		this.hangingTickLength = 40;
	}

	/* Public
	 * -------------------------------------------------- */

	public void showBetween(LocalDateTime start, LocalDateTime end) {
		this.start = start;
		this.end = end;
		this.xAxis = createXAxisLabels();
		this.repaint();
	}
	
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }

	public LocalDateTime getStart() { return this.start; }
	public LocalDateTime getEnd() { return this.end; }
	
	/* Private / Protected
	 * -------------------------------------------------- */

	private String[] createXAxisLabels() {
		Duration duration = Duration.between(start, end);
		LocalDateTime startSnapped, endSnapped;
		
		// longer periods if needed ...
		
		// DateTime rounding from: https://stackoverflow.com/a/25558595

		long days = duration.toDays();
		long hours = duration.toHours();
		long minutes = duration.toMinutes();
		if (days > 0L) {
			tickInterval = Duration.ofDays(1);
			startSnapped = start.truncatedTo(ChronoUnit.DAYS);
			if (!start.equals(startSnapped)) {
				startSnapped = startSnapped.plusDays(1);
			}
			endSnapped = end.truncatedTo(ChronoUnit.DAYS);
			tickLabelFormatter = DateTimeFormatter.ofPattern(
				"d/M/yyyy\nh:mm a"
			);
			
		} else if (hours > 0L) {
			tickInterval = Duration.ofHours(1);
			startSnapped = start.truncatedTo(ChronoUnit.HOURS);
			if (!start.equals(startSnapped)) {
				startSnapped = startSnapped.plusHours(1);
			}
			endSnapped = end.truncatedTo(ChronoUnit.HOURS);
			tickLabelFormatter = DateTimeFormatter.ofPattern("h:mm a");
			
		} else if (minutes > 0L) {
			tickInterval = Duration.ofMinutes(1);
			startSnapped = start.truncatedTo(ChronoUnit.MINUTES);
			if (!start.equals(startSnapped)) {
				startSnapped = startSnapped.plusMinutes(1);
			}
			endSnapped = end.truncatedTo(ChronoUnit.MINUTES);
			tickLabelFormatter = DateTimeFormatter.ofPattern("h:mm a");
			
		} else {
			// No tiny intervals - this is a long-term timeline
			return new String[0];
		}

		Duration durationSnapped = Duration.between(
			startSnapped, endSnapped);
		hangingStartTickInterval = Duration.between(start, startSnapped);
		hangingEndTickInterval = Duration.between(endSnapped, end);
		
		// TODO: Er ... https://stackoverflow.com/q/1590831 ... ¯\_(ツ)_/¯
		numTicks = (int) durationSnapped.dividedBy(tickInterval) + 1;
		hangingStartTick = !hangingStartTickInterval.isZero();
		hangingEndTick = !hangingEndTickInterval.isZero();
		
		int numHangingTicks = 0;
		if (hangingStartTick) numHangingTicks++;
		if (hangingEndTick) numHangingTicks++;
		
		String[] axis = new String[numTicks + numHangingTicks];
		
		LocalDateTime dateTime = start;
		int i = 0;
		if (hangingStartTick) {
			axis[i] = createTimeLabel(dateTime);
			dateTime = startSnapped;
			i++;
		}
		for (int t = 0; t < numTicks; t += 1, i += 1) {
			axis[i] = createTimeLabel(dateTime);
			dateTime = dateTime.plus(tickInterval);
		}
		if (hangingEndTick) {
			dateTime = end;
			axis[i] = createTimeLabel(dateTime);
		}

		return axis;
	}

	private String createTimeLabel(LocalDateTime dateTime) {
		return dateTime.format(tickLabelFormatter);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		if (xAxis != null) {
			g2d.setFont(
				getFont().deriveFont(11f).deriveFont(Font.BOLD)
			);
			metrics = g2d.getFontMetrics(getFont());
			
			drawXAxis(g2d);
			drawTimeline(g2d);
		}
	}

	private void drawXAxis(Graphics2D g2d) {
		if (
				tickInterval.isZero() &&
				hangingStartTickInterval.isZero() &&
				hangingEndTickInterval.isZero()
		) {
			return; // Nothing to draw
		}
		
		// Get tick interval in pixels
		double startPerNormal = ( // 0 if hangingStartTickInterval is zero
			(double) hangingStartTickInterval.toSeconds() /
			(double) tickInterval.toSeconds()
		);
		double endPerNormal = ( // 0 if hangingEndTickInterval is zero
			(double) hangingEndTickInterval.toSeconds() /
			(double) tickInterval.toSeconds()
		);
		
		double totalNormalTicks = (
			(double) numTicks - 1D // number of whole durations
			+ startPerNormal + endPerNormal // plus the fractional durations
		);
		double tickIntervalDbl = plotWidth / totalNormalTicks;
		
		tickIntervalPx = (int) Math.round(tickIntervalDbl);
		int hangingStartTickIntervalPx = (int) Math.round(
			tickIntervalDbl * startPerNormal
		);
		int hangingEndTickIntervalPx = (int) Math.round(
			tickIntervalDbl * endPerNormal
		);

		// Total axis height
		int longestTickLength = tickLength;
		if (hangingTickLength > tickLength) {
			longestTickLength = hangingTickLength;
		}
		xAxisHeight = longestTickLength + metrics.getHeight();

		// Set drawing context
		g2d.setColor(Color.BLACK);
		g2d.setStroke(axisStroke);

		// Draw the axis
		int x = margin, initialX = x;
		int y = margin + plotHeight - xAxisHeight;
		int i = 0;
		
		if (hangingStartTick) {
			drawTick(g2d, x, y, hangingTickLength, xAxis[i]);
			x += hangingStartTickIntervalPx;
			i++;
		}
		for (int t = 0; t < numTicks; t += 1, i += 1, x += tickIntervalPx) {
			drawTick(g2d, x, y, tickLength, xAxis[i]);
		}
		x -= tickIntervalPx;
		if (hangingEndTick) {
			x += hangingEndTickIntervalPx;
			drawTick(g2d, x, y, hangingTickLength, xAxis[i]);
		}
		
		g2d.drawLine(initialX, y, x, y);
	}
	
	private void drawTick(
			Graphics2D g2d, int x, int y, int length, String label
	) {
		g2d.drawLine(x, y, x, y + length);
		int labelx = x - metrics.stringWidth(label) / 2;
		int labely = y + length + metrics.getHeight();
		g2d.drawString(label, labelx, labely);
	}

	private void drawTimeline(Graphics2D g2d) {
		List<TemporalMap<Integer, ChartableEvent>> allMaps =
				timeline.getAllMaps();
		if (allMaps.size() == 0) {
			return; // nothing to draw
		}

		timelineHeight = plotHeight - xAxisHeight;
		
		int pixelsPerTimeline = timelineHeight / allMaps.size();

		g2d.setStroke(timelineStroke);

		int x = margin;
		int y = margin;
		for (TemporalMap<Integer, ChartableEvent> map : allMaps) {
			List<ChartableEvent> events = map.getBetween(
				start, end, Event.byPeriodDefaultZero, true, true
			);
			for (ChartableEvent event : events) {
				drawChartableEvent(g2d, x, y, event);
			}
			y += pixelsPerTimeline;
		}
	}

	private void drawChartableEvent(
			Graphics2D g2d, int x, int y, ChartableEvent event
	) {
		LocalDateTime startTime = event.getPeriod().start();
		LocalDateTime endTime = event.getPeriod().end();
		String label = event.getName();

		boolean startTruncated = false;
		boolean endTruncated = false;
		if (startTime.isBefore(start)) {
			// Truncate to the start, but show this has been done
			startTime = start;
			startTruncated = true;
		}
		if (endTime.isAfter(end)) {
			// Truncate to the end, but show this has been done
			endTime = end;
			endTruncated = true;
		}
		
		Duration durationToStart = Duration.between(start, startTime);
		Duration durationToEnd = Duration.between(start, endTime);
		
		double ticksToStart = (
			(double) durationToStart.toSeconds() /
			(double) tickInterval.toSeconds()
		);
		double ticksToEnd = (
			(double) durationToEnd.toSeconds() /
			(double) tickInterval.toSeconds()
		);
		
		int startx = x + (int) Math.round(
			ticksToStart * tickIntervalPx);
		int endx = x + (int) Math.round(ticksToEnd * tickIntervalPx);

		int labelx = startx;
		int labely = y - metrics.getHeight() / 2;

		// Draw
		g2d.setColor(event.getColor());
		g2d.drawLine(startx, y, endx - 1, y);
		
		g2d.setColor(Color.BLACK);
		if (startTruncated) {
			g2d.drawLine(startx, y - 5, startx, y + 5);
		}
		if (endTruncated) {
			g2d.drawLine(endx, y - 5, endx, y + 5);
		}
		
		g2d.setColor(event.getColor());
		g2d.drawString(label, labelx, labely);
	}
}
