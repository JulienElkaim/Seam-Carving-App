package imageChange;

import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javafx.scene.control.Slider;
import operation.*;
import javafx.scene.shape.Rectangle;

public class Controller {

    /** MENU:
     * 1. Attributes.
     * 2. Initialization methods.
     * 3. Listener and special-event-related methods.
     * 4. Switch methods.
     * 5. Image modification methods.
     * 6. Open & Save images methods..
     * 7. Reset & Save modifications methods.
     * 8. Seam Carving vizualization methods.
     * 9. Label actualization methods.
     */

    //Slider objects
    public Slider mySlider;
    public Label sliderListenerLabel;
    public Label sliderLabel;
    public Button directionButton;
    private String direction;
    private String sliderListener = "";

    //Hoovered objects
    public Rectangle redG;
    public Rectangle greenG;
    public Rectangle blueG;
    public Label seamPrintingLabel;
    public Label energyPrintingLabel;

    //Image objects
    public ImageView myImage;
    private BufferedImage myBufferedImage;
    private BufferedImage myBufferedImageSTOCKED;

    //Conceptual objects
    private KeyCode keyPressed;
    private String tempImg;
    private ImageResize resizer = new ImageResize();



    /**
     * INITIALIZE method is called after the fxml creation. Useful to set environment variables.
     */
    public void initialize() {
        directionButton.setText("Height");
        this.direction="H";
        this.tempImg = "null";
        this.initializeGradientItems();
        this.initializeSliderItems();


    }


    /**
     * This method initializes the sliderItems used in the application
     */
    private void initializeSliderItems() {

        this.sliderListener = "Zoom";
        this.sliderLabel.setText(this.updateSliderLabel());
        this.sliderListenerLabel.setText(this.updateListnerLabel(this.sliderListener));
        this.mySlider.valueProperty().addListener(this::ListenSlider);

    }

    /**
     * This method initialize the GradientItems used to display Gradient computation.
     */
    private void initializeGradientItems() {

        this.redG.setFill(javafx.scene.paint.Color.RED);
        this.greenG.setFill(javafx.scene.paint.Color.GREEN);
        this.blueG.setFill(javafx.scene.paint.Color.BLUE);

    }

    /**
     * Function called when the Slider's value changes, notifications are not used because we chose a "toggle framework".
     *
     * @param ov      is the observable value of the slider.
     * @param old_val is the previous value of the slider.
     * @param new_val is the actual value of the slider.
     */
    private void ListenSlider(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {

        switch (this.sliderListener) {
            case "Resize":
                this.resizeDisplayedImage(new_val.doubleValue());
                break;

            case "Zoom":
                //Just for visualisation, the use of ZOOM with the slider is done by clicking on the imageView
                break;

            case "Seam Carving":
                this.myBufferedImage = SimpleOperation.cloningBufferedImage(this.myBufferedImageSTOCKED);
                this.seamCarveDisplayedImage(new_val.intValue());
                break;

            case "Crop":
                this.cropDisplayedImage(new_val.doubleValue());
        }
    }

    /**
     * Called when the image displayed is clicked.
     *
     * @param mouseEvent is the click mouse event, with all the mouse-click relative information.
     */
    public void imageClicked(MouseEvent mouseEvent) {

        if (this.keyPressed == KeyCode.SHIFT)
            this.resetViewModifications();
        else if (this.sliderListener.equals("Zoom"))
            this.zoomDisplayedImage(mouseEvent.getX() - this.myImage.getX(), mouseEvent.getY() - this.myImage.getY(), this.mySlider.getValue());

    }

    /**
     * Called when a key is released.
     * Erase memory of this key.
     */
    public void keyPressedRemove() {
        this.keyPressed = null;
    }

    /**
     * Called when a key is pressed.
     * Set memory of this key.
     *
     * @param keyEvent is the action of the key pressed, with all the key relative information.
     */
    public void keyPressed(KeyEvent keyEvent) {
        this.keyPressed = keyEvent.getCode();
        //(CMD)CTRL+W will switch the current direction used
        if (keyEvent.getCode() == KeyCode.D && (keyEvent.isControlDown() || keyEvent.isShortcutDown()))
            this.directionSwitch();
        //(CMD)CTRL+C save modifications to continue working on it
        if (keyEvent.getCode() == KeyCode.C && (keyEvent.isControlDown() || keyEvent.isShortcutDown()) )
            this.saveViewModifications();
        //(CMD)CTRL+SHIFT+S propose to save your image.
        if (keyEvent.getCode() == KeyCode.S && keyEvent.isShiftDown() && (keyEvent.isControlDown() || keyEvent.isShortcutDown()) ){
            this.saveAFile();

        }

    }


    /**
     * Allow to switch the listener of the slider.
     *
     * @param actionEvent is the Button action, with all the button relative information.
     */
    public void switcherListener(ActionEvent actionEvent) {
        this.sliderListener = SimpleOperation.getButtonText(actionEvent.getSource().toString());
        this.sliderListenerLabel.setText(this.updateListnerLabel(this.sliderListener));
    }

    /** Actualize all the parameters tied to the direction.
     * myImage.fit(Height?Width?) -> We fix the direction not modified.
     * direction -> string showing the direction choosed. "V" is Height, "H" is width.
     * directionButto.text -> As indication, display the direction that will be modify if we click it.
     */
    public void directionSwitch() {
        if (this.direction.equals("H")) { //actually in width, go in height mode

            this.directionButton.setText("Width");
            double idealFitWidth = (this.myImage.getFitHeight()/this.myBufferedImage.getHeight()) * this.myBufferedImage.getWidth();
            this.myImage.setFitWidth(idealFitWidth);
            this.myImage.fitHeightProperty().setValue(null);
            this.direction = "V";

        }else{ // actually in height, go in width mode

            this.directionButton.setText("Height");
            double idealFitHeight = (this.myImage.getFitWidth()/this.myBufferedImage.getWidth()) * this.myBufferedImage.getHeight();
            this.myImage.setFitHeight(idealFitHeight);
            this.myImage.fitWidthProperty().setValue(null);
            this.direction = "H";
        }
        this.sliderLabel.setText(this.updateSliderLabel());
    }

    /**
     * Triggered when red square is hoovered.
     */
    public void redGSwitcher() {
        gradientSwitcher("redG");
    }

    /**
     * Triggered when green square is hoovered.
     */
    public void greenGSwitcher() {
        gradientSwitcher("greenG");
    }

    /**
     * Triggered when blue square is hoovered.
     */
    public void blueGSwitcher() {
        gradientSwitcher("blueG");
    }

    /**
     * Trigger a Gradient computation based on the color choice provided.
     *
     * @param colorGradient the color choice (RGB) to determine the Gradient used.
     */
    private void gradientSwitcher(String colorGradient) {

        if (this.tempImg.equals(colorGradient)) {
            this.myImage.setImage(SwingFXUtils.toFXImage(this.myBufferedImage, null));
            this.tempImg = "null";
        } else {

            switch (colorGradient) {
                case "redG":
                    this.redGradient();
                    break;
                case "greenG":
                    this.greenGradient();
                    break;
                case "blueG":
                    this.blueGradient();
                    break;
            }
            this.tempImg = colorGradient;
        }
    }

    /**
     * Apply a red Gradient computation on the displayed image.
     */
    private void redGradient() {
        BufferedImage img = Gradient.createGradient("red", this.myBufferedImage);
        this.myImage.setImage(SwingFXUtils.toFXImage(img, null));
    }

    /**
     * Apply a green Gradient computation on the displayed image.
     */
    private void greenGradient() {
        BufferedImage img = Gradient.createGradient("green", this.myBufferedImage);
        this.myImage.setImage(SwingFXUtils.toFXImage(img, null));
    }

    /**
     * Apply a blue Gradient computation on the displayed image.
     */
    private void blueGradient() {
        BufferedImage img = Gradient.createGradient("blue", this.myBufferedImage);
        this.myImage.setImage(SwingFXUtils.toFXImage(img, null));
    }

    /**
     * Trigger the resizing process.
     *
     * @param sliderValue is the actual value of the slider.
     */
    private void resizeDisplayedImage(double sliderValue) {

        this.myBufferedImage = SimpleOperation.cloningBufferedImage(this.resizer.resizing(this.myBufferedImageSTOCKED, sliderValue, this.direction));
        this.myImage.setImage(SwingFXUtils.toFXImage(this.myBufferedImage, null));

    }

    /**
     * Trigger the cropping process.
     *
     * @param sliderValue is the actual value of the slider.
     */
    private void cropDisplayedImage(double sliderValue) {

        this.myBufferedImage = this.resizer.cropping(this.myBufferedImageSTOCKED, sliderValue, this.direction);
        this.myImage.setImage(SwingFXUtils.toFXImage(this.myBufferedImage, null));

    }

    /**
     * Trigger zooming process.
     *
     * @param X           is the x-coordinate of the mouse pointer when click occurred.
     * @param Y           is the y-coordinate of the mouse pointer when click occurred.
     * @param sliderValue is the actual value of the slider
     */
    private void zoomDisplayedImage(double X, double Y, double sliderValue) {

        double viewHeight = this.myImage.getFitHeight();
        this.myBufferedImage = this.resizer.zoom(this.myBufferedImage, viewHeight, this.direction, X, Y, sliderValue);
        this.myImage.setImage(SwingFXUtils.toFXImage(this.myBufferedImage, null));

    }

    /**
     * Trigger Seam Carving process.
     *
     * @param nbOfSeam is the number of occurrence of the process.
     */
    private void seamCarveDisplayedImage(int nbOfSeam) {
        BufferedImage img = this.resizer.SeamCarving(nbOfSeam, this.myBufferedImage, this.direction);

        this.myBufferedImage = SimpleOperation.cloningBufferedImage(img);
        this.myImage.setImage(SwingFXUtils.toFXImage(this.myBufferedImage, null));
    }

    /**
     * Trigger Opening file process.
     *
     * @throws IOException when it fails to read the file path provided.
     */
    public void ChooseAFile() throws IOException {

        this.myBufferedImageSTOCKED = ImageIO.read(operation.SimpleOperation.imageFileOpen());
        this.myBufferedImage = SimpleOperation.cloningBufferedImage(this.myBufferedImageSTOCKED);
        this.myImage.setImage(SwingFXUtils.toFXImage(this.myBufferedImage, null));
        this.sliderListenerLabel.setText(this.updateListnerLabel(this.sliderListener));

    }

    /**
     * Trigger Saving image process.
     */
    public void saveAFile(){
        SimpleOperation.imageFileSave(this.myBufferedImage);
    }
    /**
     * Reset actual modification that occurred on the image.
     * The last persistent change saved is displayed.
     * only callable by SHIFT + LEFT CLICK
     */
    private void resetViewModifications() {

        this.myBufferedImage = SimpleOperation.cloningBufferedImage(this.myBufferedImageSTOCKED);
        this.myImage.setImage(SwingFXUtils.toFXImage(this.myBufferedImage, null));

    }

    /**
     * Save the last modification on the displayed image in order to chain modifications
     * without removing previous ones.
     */
    public void saveViewModifications() {
        int maxX = this.myBufferedImage.getWidth();
        int maxY = this.myBufferedImage.getHeight();
        this.myBufferedImageSTOCKED = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < maxY; y++)
            for (int x = 0; x < maxX; x++)
                this.myBufferedImageSTOCKED.setRGB(x, y, this.myBufferedImage.getRGB(x, y));
    }

    /**
     * Display the energy map of the displayed image and the most relevant seam.
     */
    public void imageSeamDisplay() {
        this.energyImageDisplay(this.myBufferedImage, "Show next seam", true);
    }

    /**
     * Display the energy map of the displayed image.
     */
    public void imageEnergyDisplay() {
        this.energyImageDisplay(this.myBufferedImage, "Energy computation", false);
    }

    /**
     * Method to display an energy computation on an image, with the most relevant seam if necessary.
     *
     * @param imgToEnergize the actual displayed image.
     * @param label         the label hoovered that trigger the function.
     * @param doWePrintSeam only if necessary to display the seam.
     */
    private void energyImageDisplay(BufferedImage imgToEnergize, String label, boolean doWePrintSeam) {

        this.seamPrintingLabel.setTextFill(javafx.scene.paint.Color.BLACK);

        if (this.tempImg.equals("Energy computation") || this.tempImg.equals("Show next seam")) {
            this.seamPrintingLabel.setTextFill(javafx.scene.paint.Color.PALEVIOLETRED);
            this.myImage.setImage(SwingFXUtils.toFXImage(imgToEnergize, null));
            this.tempImg = "null";

        } else {

            if (this.myBufferedImage != null)
                this.energyPrintingLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            assert this.myBufferedImage != null;
            BufferedImage bImageEnergized = ImageSeamComputation.EnergizedImage(this.myBufferedImage);

            if (doWePrintSeam) {
                int totalRedRGB = 255 << 16;
                int[] seamToPrint = ImageSeamComputation.bestSeam(bImageEnergized, this.direction);
                if (this.direction.equals("H")) {
                    for (int y = 0; y < seamToPrint.length; y++)
                        bImageEnergized.setRGB(seamToPrint[y], y, totalRedRGB);
                }else{
                    for (int x = 0; x < seamToPrint.length; x++)
                        bImageEnergized.setRGB( x,seamToPrint[x], totalRedRGB);
                }
            }

            this.myImage.setImage(SwingFXUtils.toFXImage(bImageEnergized, null));
            this.tempImg = label;
        }
    }

    /**
     * Actualize the label displayed to notify users of the actual active function.
     *
     * @param functionName is the Function actually listening the slider.
     * @return the label to display on the application window.
     */
    private String updateListnerLabel(String functionName) {
        if (this.myBufferedImage == null)
            return " Please load an Image to process ! ";
        else
            return "Actually Using : " + functionName;
    }

    /** Actualize the label displayed to notify the direction we are working on.
     *
     * @return the label to display on the application window relative to the slider
     */
    private String updateSliderLabel(){

        if (this.direction.equals("H"))
            return "Percentage of Width : ";
        if (this.direction.equals("V"))
            return "Percentage of Height : ";
        return "/!\\ Direction issue ! Please relaunch the App /!\\";

    }

}

