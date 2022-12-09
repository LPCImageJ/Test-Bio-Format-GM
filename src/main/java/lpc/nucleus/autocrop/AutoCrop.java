package lpc.nucleus.autocrop;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.plugin.ChannelSplitter;
//import ij.io.Opener;
import ij.process.*;

import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions; // Helper class for managing Bio-Formats Importer options

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors

public class AutoCrop {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final String             outputDirPath; /** the path of the directory where are saved the crop of the object */
	private final AutocropParameters autocropParameters; /** Parameters crop analyse */
	private ImagePlus        rawImg; 					/** Raw image */
	private String           imageFilePath; 			/** The path of the image to be processed */
	
	File 					 currentFile; 				/** File to process (Image input) */
	
	/**
	 * Autocrop constructor : initialisation of analyse parameter
	 *  - imageFile                 Current image analyse
	 *  - autocropParametersAnalyse List of analyse parameter
	 */		
	public AutoCrop(File imageFile, AutocropParameters autocropParametersAnalyse) 
	throws IOException, FormatException { // loci exception
		
		System.out.println("AutoCrop constructor(Input image file) : " + imageFile);
		
		this.autocropParameters = autocropParametersAnalyse;
		this.currentFile = imageFile;
		this.imageFilePath = imageFile.getAbsolutePath();
		this.outputDirPath = this.autocropParameters.getOutputFolder();

		// Use a dialog box with ImageJ
		//ImagePlus imp = IJ.openImage(this.imageFilePath);
		//ImagePlus imp = new Opener().openImage(this.imageFilePath);
        ImagePlus[] imps = null;
        
        // Bio-Formats ImporterOptions controle les otions d'ouverture des images  
        try {
         System.out.println("Trying LOCI Import Image in AutoCrop constructor");
         // Bio-Formats Import Options
         ImporterOptions options = new ImporterOptions(); 	
         options.setId(this.imageFilePath);
         options.setAutoscale(true);
         options.setStackFormat(ImporterOptions.VIEW_HYPERSTACK);
         options.setStackOrder(ImporterOptions.ORDER_XYCZT);
         options.setColorMode(ImporterOptions.COLOR_MODE_DEFAULT);
         options.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
         options.setCrop(true);
         //options.setVirtual(virtual);
         //options.setGroupFiles(true);
         
         imps = BF.openImagePlus(options); // ou BF.openImagePlus(this.imageFilePath); 
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (FormatException e) {
           e.printStackTrace();
        }
        
        // Information about the image : Detection Number of channels in this images.
		//ImagePlus[] currentImage = BF.openImagePlus(this.imageFilePath);

		// length > 1 : c0,c1,... 
		int channelNumbers = ChannelSplitter.split(imps[0]).length; // separe les canaux de la 1ere image de la stack   
      
        this.rawImg = imps[0];
		int width = this.rawImg.getWidth();
	    int height = this.rawImg.getHeight();
	    int nslices = this.rawImg.getStackSize();
	    String imtitle = this.rawImg.getTitle();
	    
	    System.out.print("AutoCrop constructor option(setAutoscale) inputFile : ");
		System.out.println(imtitle + " " + width + " " + height + " " + nslices);
	    
		System.out.println("AutoCrop constructor bitDepth   = " + this.rawImg.getBitDepth());
		System.out.println("AutoCrop constructor NdOfDims   = " + this.rawImg.getNDimensions());
		System.out.println("AutoCrop constructor Type       = " + this.rawImg.getType());
		System.out.println("AutoCrop constructor NbSlices   = " + this.rawImg.getNSlices());
		System.out.println("AutoCrop constructor NbFrames   = " + this.rawImg.getNFrames());
		System.out.println("AutoCrop constructor NbChannels = " + channelNumbers);
		
		if (this.rawImg.getType() != ImagePlus.GRAY8) {
		  // In order to support 32bit images, pict[] must be changed to float[], and  getPixel(x, y);
	      IJ.error("8 bit greyscale image required. Image requires a 8-bit conversion.");
	    } else
			IJ.error(imtitle+" is a 8 bit greyscale image. No conversion needed.");
		
		this.rawImg.show();
		
		// Ouverture sans bio-format (ImageJ)
		getImage().show();
		
		if (this.rawImg.getNDimensions() > 3) { // Nb de dimensions (2,3,4,5)
		    IJ.error("3D stacks are currently not supported");
		    return;
		} else
			IJ.error(imtitle+" is a accepted 3D stack.");
		
		if (imps[0].isComposite()) { // image multi-canaux
		    int z = imps[0].getSlice();
		    System.out.println("Number of Channels = " + z);
		    } 
		else    
			  IJ.error("Split Channels", "Multichannel image required");
	}
	
	/**
	 * AutoCrop.java : Charger une image sans bio-format
	 */
	public ImagePlus getImage() throws IOException {
		ImagePlus currentImage = IJ.openImage(this.imageFilePath);
		return currentImage;
	}	
}

/*
 // Image Bit-depth must be 8-bit
 if (this.rawImg.getBitDepth() > 8) { // Returns the bit depth, 8, 16, 24 (RGB) or 32.
 	 throw new IllegalArgumentException("Unsupported conversion");
 } else { // 2D only
	ImageConverter imageConverter = new ImageConverter(rawImg);
	imageConverter.convertToGray8();
 }
*/