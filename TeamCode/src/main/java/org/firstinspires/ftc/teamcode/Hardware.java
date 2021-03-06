package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public class Hardware extends LinearOpMode {


    // Good Luck!
    //You should put constants here

    protected DcMotor leftTread, rightTread, carousel, pulley, leftIntake, rightIntake;
    protected Servo leftBucket, rightBucket;
    static final double     COUNTS_PER_MOTOR_REV    = 1680 ;    // Needs to be fixed based on the motors
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference. Not sure what it is
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * Math.PI);
    // TODO: Needs tuning
    static final double ARM_MOVE_REVOLUTIONS = 5;
    static final double ARM_ROTATE_REVOLUTIONS = 5;
    static final double CAROUSEL_POWER = 0.5;
    //Use the following if the carousel motor is not encoded
    static final long CAROUSEL_MILLIS = 1000;
    /* Use the following if the carousel motor is encoded
    static final double CAROUSEL_INCHES_TO_TURN = 50; // Just over the circumference of the carousel
    static final double CAROUSEL_DIAMETER_INCHES = 3; //Might need to be adjusted
    static final double CAROUSEL_MOTOR_REVOLUTIONS = CAROUSEL_INCHES_TO_TURN / (CAROUSEL_DIAMETER_INCHES * Math.PI); */

    public ElapsedTime runtime = new ElapsedTime();

    private BNO055IMU imu;
    double globalAngle, rotation;
    Orientation lastAngles = new Orientation();





    // Setup your drivetrain (Define your motors etc.)
    public void hardwareSetup() {

        // Initialize driving motors
        leftTread = hardwareMap.dcMotor.get("leftTread");
        rightTread = hardwareMap.dcMotor.get("rightTread");

        //Set Driving Directions
        leftTread.setDirection(DcMotor.Direction.FORWARD);
        rightTread.setDirection(DcMotor.Direction.REVERSE);

        //Initialize and set direction of carousel motor
        carousel = hardwareMap.dcMotor.get("carousel");

        // TODO: flip the direction if it's going backwards
        carousel.setDirection(DcMotor.Direction.FORWARD);

        leftIntake = hardwareMap.dcMotor.get("leftIntake");
        rightIntake = hardwareMap.dcMotor.get("rightIntake");
        pulley = hardwareMap.dcMotor.get("pulley");
        leftBucket = hardwareMap.servo.get("leftBucket");
        rightBucket = hardwareMap.servo.get("rightBucket");

        //set motor directions
//        frontLeft.setDirection(DcMotor.Direction.REVERSE);
//        frontRight.setDirection(DcMotor.Direction.FORWARD);
//        backRight.setDirection(DcMotor.Direction.FORWARD);
//        backLeft.setDirection(DcMotor.Direction.REVERSE);
        carousel.setDirection(DcMotor.Direction.FORWARD);
        leftBucket.setDirection(Servo.Direction.FORWARD);
        rightBucket.setDirection(Servo.Direction.REVERSE);
        leftIntake.setDirection(DcMotorSimple.Direction.FORWARD);
        rightIntake.setDirection(DcMotorSimple.Direction.FORWARD);
        pulley.setDirection(DcMotorSimple.Direction.REVERSE);

        //set all motors to actively brake when assigned power is 0
        rightTread.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftTread.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        carousel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftIntake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightIntake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pulley.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftTread.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightTread.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftIntake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightIntake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        pulley.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Use encoders to regulate speed
        leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftIntake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightIntake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        pulley.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //update telemetry
        telemetry.addData("Status:", "Setup Complete");
        telemetry.update();
    }
    public void imuSetup() {

        telemetry.addData("Status","Setting Up IMU");
        telemetry.update();
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.mode = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = false;
        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
//        imu = hardwareMap.get(BNO055IMU.class, "imu");
//        imu = hardwareMap.i2cDevice.get("imu");
        imu = hardwareMap.get(BNO055IMU.class,"imu");
        imu.initialize(parameters);

        // Create a pid controller with some guess values

        telemetry.addData("Status","Calibrating Gyro");
        telemetry.update();
        // make sure the imu gyro is calibrated before continuing.
        while (!isStopRequested() && !imu.isGyroCalibrated()) {
            sleep(50);
            idle();
        }

        telemetry.addData("Status:", "IMU Setup Complete");
        telemetry.update();
    }

    public float getHeading() {
        return this.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
    }

    //TODO: test to see if this works
    public void rotateToPos(double degrees, double maxPower) {
        PIDController pidRotate = new PIDController(.004, .00002, 0);
        pidRotate.setSetpoint(degrees);
        pidRotate.setInputRange(0, 180);
        pidRotate.setOutputRange(0, maxPower);
        pidRotate.setTolerance(0.5);
        pidRotate.enable();
        pidRotate.performPID();
        while(opModeIsActive() && !pidRotate.onTarget()) {
            double power = pidRotate.performPID(getHeading());
            setDrivingPowers(-power, power);
            telemetry.addData("Status","Rotating to " + degrees);
            telemetry.addData("Currently at",getHeading());
            telemetry.update();
        }
    }
    public void setIntakePower (double power){
        leftIntake.setPower(power);
        rightIntake.setPower(-power);
    }

    //Careful - the while loop freezes the robot
    public void encoderToSpecificPos(DcMotor motor, int pos , double power){
        motor.setTargetPosition(pos);
        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motor.setPower(power);
        telemetry.addData("strafe", motor.getCurrentPosition());
        telemetry.update();

        while (motor.isBusy() && opModeIsActive()){
            idle();
        }

        motor.setPower(0);
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void setDrivingPowers(double leftPower, double rightPower) {
        leftTread.setPower(leftPower);
        rightTread.setPower(rightPower);
    }

    public void driveForward(final double power) {
        setDrivingPowers(power, power);
    }

    public void deliverDuck() {
        //Use the following if the motor is not encoded
        carousel.setPower(CAROUSEL_POWER);
        telemetry.addData("Status","Delivering Duck/TSE");
        telemetry.update();
        sleep(CAROUSEL_MILLIS);
        carousel.setPower(0);
        telemetry.addData("Status","Finished Delivery");
        telemetry.update();
        //Use the following if the motor is encoded
        //singleMotorEncoderDrive(carousel, CAROUSEL_POWER, REVOLUTIONS_TO_TURN, 10);
    }

    public void singleMotorEncoderDrive(DcMotor motor, double power, double revolutions, int timeoutS) {
        double newMotorTarget;
        if (opModeIsActive()) {
            //calculate target positions
            newMotorTarget = motor.getCurrentPosition() + revolutions * COUNTS_PER_MOTOR_REV;
            //set target positions
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motor.setTargetPosition((int) newMotorTarget);
            //restart timer and set power
            runtime.reset();
            motor.setPower(power);
            //Display telemetry
            telemetry.addData("Status", "Moving motor to position");
            telemetry.update();
            //loop until the motor reaches target position
            while (opModeIsActive() && motor.isBusy() && runtime.seconds() < timeoutS) {
                // do nothing
                idle();
            }
            //Stop motor
            motor.setPower(0);
            //Get out of position mode
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            //Clear Telemetry
            telemetry.update();
        }
    }


    //stop a motor if it's reached its position
    public void updateMotor(DcMotor motor) {
        if(motor.getMode() == DcMotor.RunMode.RUN_TO_POSITION && !motor.isBusy()) {
            motor.setPower(0);
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public void encoderDrive(double maxPower, double rightInches, double leftInches){
        // stop and reset the encoders? Maybe not. Might want to get position and add from there
        double rightTarget;
        double leftTarget;

        if (opModeIsActive()){
            //calculate and set target positions

            rightTarget = rightTread.getCurrentPosition()     +  (rightInches * COUNTS_PER_INCH);
            leftTarget = leftTread.getCurrentPosition()     +  (leftInches * COUNTS_PER_INCH);

            rightTread.setTargetPosition((int)(rightTarget));
            leftTread.setTargetPosition((int)(leftTarget));

            // Run to position
            rightTread.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftTread.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // Set powers. For now I'm setting to maxPower, so be careful.
            // In the future I'd like to add some acceleration control through powers, which
            // should help with encoder accuracy. Stay tuned.
            runtime.reset();
            rightTread.setPower(maxPower);
            leftTread.setPower(maxPower);

            while (opModeIsActive() &&
                    (rightTread.isBusy() || leftTread.isBusy())) {
                idle();
                if(!rightTread.isBusy()){
                    rightTread.setPower(0);
                    rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                }
                if(!leftTread.isBusy()){
                    leftTread.setPower(0);
                    leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


                }
            }
            // Set Zero Power
            rightTread.setPower(0);
            leftTread.setPower(0);

            // Go back to Run_Using_Encoder
            leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }


    }

    public void encoderDriveAnd(double maxPower, double frontRightInches, double frontLeftInches, double backLeftInches, double backRightInches){
        // stop and reset the encoders? Maybe not. Might want to get position and add from there
        double newFRTarget;
        double newFLTarget;
        double newBLTarget;
        double newBRTarget;

        if (opModeIsActive()){
            //calculate and set target positions

            newFRTarget = rightTread.getCurrentPosition()     +  (frontRightInches * COUNTS_PER_INCH);
            newFLTarget = leftTread.getCurrentPosition()     +  (frontLeftInches * COUNTS_PER_INCH);

            rightTread.setTargetPosition((int)(newFRTarget));
            leftTread.setTargetPosition((int)(newFLTarget));

            // Run to position
            rightTread.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftTread.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // Set powers. For now I'm setting to maxPower, so be careful.
            // In the future I'd like to add some acceleration control through powers, which
            // should help with encoder accuracy. Stay tuned.
            runtime.reset();
            rightTread.setPower(maxPower);
            leftTread.setPower(maxPower);

            while (opModeIsActive() &&
                    (rightTread.isBusy() && leftTread.isBusy())) {
                idle();

//                if(!frontRight.isBusy()){
//                    frontLeft.setPower(0);
//                }
//                if(!frontLeft.isBusy()){
//                    frontLeft.setPower(0);
//                }
//                if(!backRight.isBusy()){
//                    backRight.setPower(0);
//                }
//                if(!backLeft.isBusy()){
//                    backLeft.setPower(0);
//                }

            }
            // Set Zero Power
            rightTread.setPower(0);
            leftTread.setPower(0);

            // Go back to Run_Using_Encoder
            leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }


    }

    public void setBucketPosition(double bucketPosition){
        leftBucket.setPosition(bucketPosition);
        rightBucket.setPosition(bucketPosition);
    }


    // Last thing is an empty runOpMode because it's a linearopmode
    @Override
    public void runOpMode() throws InterruptedException {

    }
}
