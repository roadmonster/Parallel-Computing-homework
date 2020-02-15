/*
 * Original Author Kevin Lundeen
 * Pirated by Hao Li
 * CPSC 5600, Seattle University
 * This is free and unencumbered software released into the public domain.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * HW5 class
 * Initiate the interface
 * generates and reads a data file of hit records
 * Create a HeatScan object to calculate
 * Read the data from the object and paint it.
 */
public class HW5 {
	private static final int DIM = 150;
	private static final String REPLAY = "Replay";
	private static JFrame application;
	private static JButton button;
	private static Color[][] grid;
	private static ArrayList<Integer[][]> heatMap = new ArrayList<>();
	private static final int HOT_CALIB = 3;
	private static int current;
	private static final String FILE_NAME = "observation_test.dat";
	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		grid = new Color[DIM][DIM];
		application = new JFrame();
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fillGrid(grid);
		
		ColoredGrid gridPanel = new ColoredGrid(grid);
		application.add(gridPanel, BorderLayout.CENTER);
		
		button = new JButton(REPLAY);
		button.addActionListener(new BHandler());
		application.add(button, BorderLayout.PAGE_END);
		
		application.setSize(DIM * 4, (int)(DIM * 4.4));
		application.setVisible(true);
		application.repaint();

		Observation.generateFile(FILE_NAME);

		ArrayList<Observation> rawData = new ArrayList<>();

		Observation.readFiles(FILE_NAME,rawData);

		HeatScan hs = new HeatScan(rawData);
		Integer[][]final_result = hs.getReduction(0);
		current = 0;
		hs.getScan(heatMap);


		animate();
	}
	
	private static void animate() throws InterruptedException {
		button.setEnabled(false);
 		for (current = 0; current < heatMap.size(); current++) {
			fillGrid(grid);
			application.repaint();
			Thread.sleep(50);
		}
		button.setEnabled(true);
		application.repaint();
	}
	
	static class BHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (REPLAY.equals(e.getActionCommand())) {
				new Thread() {
			        public void run() {
			            try {
								animate();
							} catch (InterruptedException e) {
								System.exit(0);
							}
			        }
			    }.start();
			}
		}
	};

	static private final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.RED;


	private static void fillGrid(Color[][] grid) {
		
		Integer[][]temp = new Integer[DIM][DIM];
		for (int r = 0; r < grid.length; r++)
			for (int c = 0; c < grid[r].length; c++) {
				temp = heatMap.get(current);
				grid[r][c] = interpolateColor(temp[r][c] / HOT_CALIB, COLD, HOT);
			}



	}
	
	private static Color interpolateColor(double ratio, Color a, Color b) {
		int ax = a.getRed();
		int ay = a.getGreen();
		int az = a.getBlue();
		int cx = ax + (int) ((b.getRed() - ax) * ratio);
		int cy = ay + (int) ((b.getGreen() - ay) * ratio);
		int cz = az + (int) ((b.getBlue() - az) * ratio);
		return new Color(cx, cy, cz);
	}

}
