package org.firstinspires.ftc.teamcode.drive;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.acmerobotics.roadrunner.drive.DriveSignal;
import com.acmerobotics.roadrunner.drive.MecanumDrive;
import com.acmerobotics.roadrunner.followers.HolonomicPIDVAFollower;
import com.acmerobotics.roadrunner.followers.TrajectoryFollower;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.acmerobotics.roadrunner.trajectory.constraints.AngularVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.MinVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequenceBuilder;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequenceRunner;
import org.firstinspires.ftc.teamcode.util.LynxModuleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.MAX_ACCEL;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.MAX_ANG_ACCEL;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.MAX_ANG_VEL;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.MAX_VEL;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.MOTOR_VELO_PID;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.RUN_USING_ENCODER;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.TRACK_WIDTH;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.encoderTicksToInches;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.kA;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.kStatic;
import static org.firstinspires.ftc.teamcode.drive.DriveConstants.kV;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.DirLF;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.DirLR;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.DirRF;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.DirRR;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.DirWooble;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.full_power;
//import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.organizatorInchis;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.pozitie_gheara_closed;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.pozitie_gheara_open;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.pozitie_inceput_deschis;
//import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.putereAbsorbtie;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.putereWooble;
//import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.putere_fata_dr;
//import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.putere_fata_st;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.tragaciDeschis;
import static org.firstinspires.ftc.teamcode.drive.ThoborVARS.tragaciInchis;

/*
 * Simple mecanum drive hardware implementation for REV hardware.
 */
@Config
public class SampleMecanumDrive extends MecanumDrive {
    private static final TrajectoryVelocityConstraint VEL_CONSTRAINT = getVelocityConstraint(MAX_VEL, MAX_ANG_VEL, TRACK_WIDTH);
    private static final TrajectoryAccelerationConstraint ACCEL_CONSTRAINT = getAccelerationConstraint(MAX_ACCEL);
    public static PIDCoefficients TRANSLATIONAL_PID = new PIDCoefficients(8, 0, 0);
    public static PIDCoefficients HEADING_PID = new PIDCoefficients(10, 0, 0);
    public static double LATERAL_MULTIPLIER = 1.252;
    public static double VX_WEIGHT = 1;
    public static double VY_WEIGHT = 1;
    public static double OMEGA_WEIGHT = 1;
    //    private DcMotorEx leftFront, leftRear, rightRear, rightFront;
    public static DcMotorEx leftFront;
    public static DcMotorEx leftRear;
    public static DcMotorEx rightRear;
    public static DcMotorEx rightFront;
    public static DcMotorEx aruncare;
    public static RevTouchSensor buton_servo;
    public static RevTouchSensor buton_capat;
    public static Servo stanga;
    public static Servo dreapta;
    public static Servo wooble_gheara;
    public static ElapsedTime timpAr = new ElapsedTime();
    public DcMotorEx absorbtie;

    public CRServo wooblemoto;
    private TrajectorySequenceRunner trajectorySequenceRunner;
    private TrajectoryFollower follower;
    private Servo tragaci;
    private List<DcMotorEx> motors;
    private BNO055IMU imu;
    private VoltageSensor batteryVoltageSensor;


    private Servo inceput;


    public SampleMecanumDrive(HardwareMap hardwareMap) {
        super(kV, kA, kStatic, TRACK_WIDTH, TRACK_WIDTH, LATERAL_MULTIPLIER);

        follower = new HolonomicPIDVAFollower(TRANSLATIONAL_PID, TRANSLATIONAL_PID, HEADING_PID,
                new Pose2d(0.5, 0.5, Math.toRadians(5.0)), 0.5);

        LynxModuleUtil.ensureMinimumFirmwareVersion(hardwareMap);

        batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();

        for (LynxModule module : hardwareMap.getAll(LynxModule.class)) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        // TODO: adjust the names of the following hardware devices to match your configuration
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
//        parameters.mode = BNO055IMU.SensorMode.GYRONLY;
        parameters.calibrationDataFile = "HANGA_PROBLEMA.json";
        imu.initialize(parameters);

        // TODO: if your hub is mounted vertically, remap the IMU axes so that the z-axis points
        // upward (normal to the floor) using a command like the following:
        // BNO055IMUUtil.remapAxes(imu, AxesOrder.XYZ, AxesSigns.NPN);

        leftFront = hardwareMap.get(DcMotorEx.class, "front_left_motor");
        leftRear = hardwareMap.get(DcMotorEx.class, "back_left_motor");
        rightRear = hardwareMap.get(DcMotorEx.class, "back_right_motor");
        rightFront = hardwareMap.get(DcMotorEx.class, "front_right_motor");

        buton_servo = hardwareMap.get(RevTouchSensor.class, "servo");
        buton_capat = hardwareMap.get(RevTouchSensor.class, "capat");


        absorbtie = hardwareMap.get(DcMotorEx.class, "absorbtie");
        aruncare = hardwareMap.get(DcMotorEx.class, "aruncare");

        tragaci = hardwareMap.get(Servo.class, "tragaci");


        wooblemoto = hardwareMap.get(CRServo.class, "wooblemoto");

        stanga = hardwareMap.get(Servo.class, "stanga");
        dreapta = hardwareMap.get(Servo.class, "dreapta");


        motors = Arrays.asList(leftFront, leftRear, rightRear, rightFront);


        if (DirWooble == 1) wooblemoto.setDirection(DcMotorEx.Direction.REVERSE);
        else wooblemoto.setDirection(DcMotorEx.Direction.FORWARD);

        wooble_gheara = hardwareMap.get(Servo.class, "wooble_gheara");


        inceput = hardwareMap.get(Servo.class, "inceput");


        if (DirLR == 1) leftRear.setDirection(DcMotorEx.Direction.FORWARD);
        else leftRear.setDirection(DcMotorEx.Direction.REVERSE);
        if (DirRR == 1) rightRear.setDirection(DcMotorEx.Direction.FORWARD);
        else rightRear.setDirection(DcMotorEx.Direction.REVERSE);
        if (DirLF == 1) leftFront.setDirection(DcMotorEx.Direction.FORWARD);
        else leftFront.setDirection(DcMotorEx.Direction.REVERSE);
        if (DirRF == 1) rightFront.setDirection(DcMotorEx.Direction.FORWARD);
        else rightFront.setDirection(DcMotorEx.Direction.REVERSE);

        motors = Arrays.asList(leftFront, leftRear, rightRear, rightFront);

        aruncare.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        aruncare.setDirection(DcMotorEx.Direction.REVERSE);
        aruncare.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        PozitiiDeBaza();

        for (DcMotorEx motor : motors) {
            MotorConfigurationType motorConfigurationType = motor.getMotorType().clone();
            motorConfigurationType.setAchieveableMaxRPMFraction(1.0);
            motor.setMotorType(motorConfigurationType);
        }

        if (RUN_USING_ENCODER) {
            setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        if (RUN_USING_ENCODER && MOTOR_VELO_PID != null) {
            setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, MOTOR_VELO_PID);
        }

        // TODO: reverse any motors using DcMotor.setDirection()

        // TODO: if desired, use setLocalizer() to change the localization method
        // for instance, setLocalizer(new ThreeTrackingWheelLocalizer(...));
        setLocalizer(new TwoWheelTrackingLocalizer(hardwareMap, this));

        trajectorySequenceRunner = new TrajectorySequenceRunner(follower, HEADING_PID);
    }

    public static void WoobleGheara(double pozgheara) {
        wooble_gheara.setPosition(pozgheara);
    }

    public static void Arunca(double puterearuncare) {
        aruncare.setPower(puterearuncare);
    }

    public static void opresteMotoare() {
        leftRear.setMotorDisable();
        rightRear.setMotorDisable();
        leftFront.setMotorDisable();
        rightFront.setMotorDisable();
    }

    public static void pornesteMotoare() {
        leftRear.setMotorEnable();
        rightRear.setMotorEnable();
        leftFront.setMotorEnable();
        rightFront.setMotorEnable();
    }

    public static TrajectoryVelocityConstraint getVelocityConstraint(double maxVel, double maxAngularVel, double trackWidth) {
        return new MinVelocityConstraint(Arrays.asList(
                new AngularVelocityConstraint(maxAngularVel),
                new MecanumVelocityConstraint(maxVel, trackWidth)
        ));
    }

    public static TrajectoryAccelerationConstraint getAccelerationConstraint(double maxAccel) {
        return new ProfileAccelerationConstraint(maxAccel);
    }

    public TrajectoryBuilder trajectoryBuilder(Pose2d startPose) {
        return new TrajectoryBuilder(startPose, VEL_CONSTRAINT, ACCEL_CONSTRAINT);
    }

    public TrajectoryBuilder trajectoryBuilder(Pose2d startPose, boolean reversed) {
        return new TrajectoryBuilder(startPose, reversed, VEL_CONSTRAINT, ACCEL_CONSTRAINT);
    }

    public TrajectoryBuilder trajectoryBuilder(Pose2d startPose, double startHeading) {
        return new TrajectoryBuilder(startPose, startHeading, VEL_CONSTRAINT, ACCEL_CONSTRAINT);
    }

    public TrajectorySequenceBuilder trajectorySequenceBuilder(Pose2d startPose) {
        return new TrajectorySequenceBuilder(
                startPose,
                VEL_CONSTRAINT, ACCEL_CONSTRAINT,
                MAX_ANG_VEL, MAX_ANG_ACCEL
        );
    }

    public void turnAsync(double angle) {
        trajectorySequenceRunner.followTrajectorySequenceAsync(
                trajectorySequenceBuilder(getPoseEstimate())
                        .turn(angle)
                        .build()
        );
    }

    public void turn(double angle) {
        turnAsync(angle);
        waitForIdle();
    }

    public void PozitiiDeBaza() {
        TragaciPozitie(tragaciInchis);
//        OrganizatorPozitie(organizatorInchis);
        InceputPozitie(pozitie_inceput_deschis);
        WoobleGheara(pozitie_gheara_closed);
    }

    public void cancelFollowing() {
        waitForIdle();
    }

    public void InceputPozitie(double pozitieinceput) {
        inceput.setPosition(pozitieinceput);
    }

    public void TragaciPozitie(double poztragaci) {
        tragaci.setPosition(poztragaci);
    }

    public void OrganizatorPozitie(double pozorganizator) {
//        organizator.setPosition(pozorganizator);
    }

//    public void fatete(double putere_fata_dr, double putere_fata_st) {
//        fata_dr.setPower(putere_fata_dr);
//        fata_st.setPower(putere_fata_st);
//    }

    public void WoobleFinal() throws InterruptedException {
        this.RidicaWooble(1);
        sleep(2500);
        this.RidicaWooble(0);
        sleep(300);
        this.Gheara(true);

        this.RidicaWooble(-1);
        sleep(1500);
        this.RidicaWooble(0);
    }

    public void RidicaWooble(double putereridicare) {
        wooblemoto.setPower(putereridicare);
    }

    //    public void PowerShot(Pose2d p) throws InterruptedException {
    public void PowerShot() throws InterruptedException {
        opresteMotoare();
        double gr = 2;

        for (int i = 0; i < 3; i++) {
            timpAr.reset();
            double x;
            if (i == 0) x = 2;
            else
                x = 1.2;
            while (timpAr.time() < x) {
                if (i == 0)
                    Arunca(0.739);
                else if (i == 1)
                    Arunca(0.712);
                else {
                    Arunca(0.666);
                }
            }
            if (i == 0) sleep(250);
            if (i == 1) sleep(343);

            TragaciPozitie(tragaciDeschis);
            sleep(200);
            TragaciPozitie(tragaciInchis);

            if (i == 1) {
                Trajectory strafe = this.trajectoryBuilder(getPoseEstimate())
                        .strafeLeft(7.4,
                                SampleMecanumDrive.getVelocityConstraint(5, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                        .build();
                this.followTrajectory(strafe);
            } else if (i != 2) {
                Trajectory strafe = this.trajectoryBuilder(getPoseEstimate())
//                        .strafeLeft(6.38,
                        .strafeLeft(6.43,
                                SampleMecanumDrive.getVelocityConstraint(5, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                        .build();
                this.followTrajectory(strafe);
            }
//            turn(Math.toRadians(gr));
            gr = gr + 2.3;
        }
        Arunca(0);

        pornesteMotoare();
    }

    public void AruncaAutomat(int nr_inele) throws InterruptedException {
        WoobleGheara(pozitie_gheara_closed);
        opresteMotoare();

        for (int i = 0; i < nr_inele; i++) {
            timpAr.reset();
            while (timpAr.time() < 1.35) {
                if (i == 0) {
                    Arunca(0.795);
                } else {
                    Arunca(0.75);
                }
            }
            sleep(650);
            TragaciPozitie(tragaciDeschis);
            sleep(250);
            TragaciPozitie(tragaciInchis);
            Arunca(0);
        }

        pornesteMotoare();
        WoobleGheara(pozitie_gheara_open);

    }

//    public void AruncaAutomat(int nr_inele) throws InterruptedException {
//
//
//    }



    ElapsedTime timpWoob = new ElapsedTime();

    public void DuWooble(int pas) throws InterruptedException {
        timpWoob.reset();
        if (pas == 1) {
            do {
                RidicaWooble(putereWooble);
            } while (!buton_servo.isPressed());
            sleep(100);
            WoobleGheara(pozitie_gheara_open);
            sleep(100);

            RidicaWooble(-1);
            do {
                RidicaWooble(-putereWooble);
                if (timpWoob.time() > 5) break;
            } while (!buton_capat.isPressed());
//            RidicaWooble(-0.1);
//            sleep(200);
            RidicaWooble(0);
        } else if (pas == 2) {

            WoobleGheara(pozitie_gheara_open);
            do {
                RidicaWooble(putereWooble);
                if (timpWoob.time() > 2.5) break;
            } while (!buton_servo.isPressed());
            RidicaWooble(0.3);
            sleep(300);
            WoobleGheara(pozitie_gheara_closed);
            sleep(500);

            RidicaWooble(-1);

//            do {
//                RidicaWooble(-putereWooble);
//            } while (!buton_capat.isPressed());
//            RidicaWooble(-0.3);
//            sleep(100);
//            RidicaWooble(-0.1);
            sleep(400);
            RidicaWooble(0);
        } else if (pas == 3) {
            while (true) {
//                Absoarbe(putereAbsorbtie);
//                fatete(putere_fata_dr, putere_fata_st);
                RidicaWooble(putereWooble);
                if (buton_servo.isPressed()) {
                    WoobleGheara(pozitie_gheara_open);
                    //noinspection BusyWait
                    sleep(33);
                    break;
                }
            }
            RidicaWooble(-1);
            sleep(1000);

            RidicaWooble(0);

        } else if (pas == 4) {
            WoobleGheara(pozitie_gheara_closed);
            sleep(500);
            RidicaWooble(-putereWooble);
            do {
                RidicaWooble(-putereWooble);
            } while (!buton_capat.isPressed());
            RidicaWooble(-0.1);
            sleep(150);
            RidicaWooble(0);

        } else if (pas == 5) {
            do {
                RidicaWooble(putereWooble);
            } while (!buton_servo.isPressed());
            RidicaWooble(0.3);
            sleep(150);
            RidicaWooble(0.1);
            sleep(250);
            WoobleGheara(pozitie_gheara_open);
            sleep(250);

            RidicaWooble(-1);

            do {
                RidicaWooble(-putereWooble);
            } while (!buton_capat.isPressed());

            RidicaWooble(-0.3);
            sleep(150);
            RidicaWooble(-0.1);
            sleep(200);
            RidicaWooble(0);

        } else if (pas == 6) {
            do {
                RidicaWooble(putereWooble);
            } while (!buton_servo.isPressed());
            RidicaWooble(0.2);
            sleep(250);
            WoobleGheara(pozitie_gheara_open);
            sleep(250);

            RidicaWooble(-1);

            do {
                RidicaWooble(-putereWooble);
            } while (!buton_capat.isPressed());

//            RidicaWooble(-0.3);
//            sleep(150);
//            RidicaWooble(-0.1);
//            sleep(200);
            RidicaWooble(0);
        } else if (pas == 7) {
            WoobleGheara(pozitie_gheara_open);
            sleep(300);
            RidicaWooble(-1);

            do {
                RidicaWooble(-putereWooble);
                if (timpWoob.time() > 2.5) break;
            } while (!buton_capat.isPressed());

//            RidicaWooble(-0.3);
//            sleep(150);
//            RidicaWooble(-0.1);
//            sleep(200);
            RidicaWooble(0);
        } else if (pas == 8) {
            WoobleGheara(pozitie_gheara_open);
            sleep(300);

            RidicaWooble(1);
            do {
                RidicaWooble(-putereWooble);
            } while (!buton_capat.isPressed());

            sleep(500);
            WoobleGheara(pozitie_gheara_closed);
            RidicaWooble(-1);
            sleep(1000);
//            RidicaWooble(-0.3);
//            sleep(150);
//            RidicaWooble(-0.1);
//            sleep(200);
        }
    }

    private boolean okey = true;

    public void Gheara(boolean ok) {
        if (okey && !ok) {
            stanga.setPosition(0);
            dreapta.setPosition(0);
            okey = false;
        } else {
            stanga.setPosition(1);
            dreapta.setPosition(1);
            okey = true;
        }
    }





    public void Gheara() {
        Gheara(false);
    }


    public void Absoarbe(double putereabsorbtie) {
        absorbtie.setPower(putereabsorbtie);
    }

    public void setMotorPowersFull(double v, double v1, double v2, double v3) {
        leftFront.setPower(v * full_power);
        leftRear.setPower(v1 * full_power);
        rightRear.setPower(v2 * full_power);
        rightFront.setPower(v3 * full_power);

    }

    public void followTrajectoryAsync(Trajectory trajectory) {
        trajectorySequenceRunner.followTrajectorySequenceAsync(
                trajectorySequenceBuilder(trajectory.start())
                        .addTrajectory(trajectory)
                        .build()
        );
    }

    public void followTrajectory(Trajectory trajectory) {
        followTrajectoryAsync(trajectory);
        waitForIdle();
    }

    public void followTrajectorySequenceAsync(TrajectorySequence trajectorySequence) {
        trajectorySequenceRunner.followTrajectorySequenceAsync(trajectorySequence);
    }

    public void followTrajectorySequence(TrajectorySequence trajectorySequence) {
        followTrajectorySequenceAsync(trajectorySequence);
        waitForIdle();
    }

    public Pose2d getLastError() {
        return trajectorySequenceRunner.getLastPoseError();
    }

    public void update() {
        updatePoseEstimate();
        DriveSignal signal = trajectorySequenceRunner.update(getPoseEstimate(), getPoseVelocity());
        if (signal != null) setDriveSignal(signal);
    }

    public void waitForIdle() {
        while (!Thread.currentThread().isInterrupted() && isBusy())
            update();
    }

    public boolean isBusy() {
        return trajectorySequenceRunner.isBusy();
    }

    public void setMode(DcMotor.RunMode runMode) {
        for (DcMotorEx motor : motors) {
            motor.setMode(runMode);
        }
    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        for (DcMotorEx motor : motors) {
            motor.setZeroPowerBehavior(zeroPowerBehavior);
        }
    }

    public void setPIDFCoefficients(DcMotor.RunMode runMode, PIDFCoefficients coefficients) {
        PIDFCoefficients compensatedCoefficients = new PIDFCoefficients(
                coefficients.p, coefficients.i, coefficients.d,
                coefficients.f * 12 / batteryVoltageSensor.getVoltage()
        );

        for (DcMotorEx motor : motors) {
            motor.setPIDFCoefficients(runMode, compensatedCoefficients);
        }
    }

    public void setWeightedDrivePower(Pose2d drivePower) {
        Pose2d vel = drivePower;

        if (Math.abs(drivePower.getX()) + Math.abs(drivePower.getY())
                + Math.abs(drivePower.getHeading()) > 1) {
            // re-normalize the powers according to the weights
            double denom = VX_WEIGHT * Math.abs(drivePower.getX())
                    + VY_WEIGHT * Math.abs(drivePower.getY())
                    + OMEGA_WEIGHT * Math.abs(drivePower.getHeading());

            vel = new Pose2d(
                    VX_WEIGHT * drivePower.getX(),
                    VY_WEIGHT * drivePower.getY(),
                    OMEGA_WEIGHT * drivePower.getHeading()
            ).div(denom);
        }

        setDrivePower(vel);
    }

    @NonNull
    @Override
    public List<Double> getWheelPositions() {
        List<Double> wheelPositions = new ArrayList<>();
        for (DcMotorEx motor : motors) {
            wheelPositions.add(encoderTicksToInches(motor.getCurrentPosition()));
        }
        return wheelPositions;
    }

    @Override
    public List<Double> getWheelVelocities() {
        List<Double> wheelVelocities = new ArrayList<>();
        for (DcMotorEx motor : motors) {
            wheelVelocities.add(encoderTicksToInches(motor.getVelocity()));
        }
        return wheelVelocities;
    }

    @Override
    public void setMotorPowers(double v, double v1, double v2, double v3) {
        leftFront.setPower(v);
        leftRear.setPower(v1);
        rightRear.setPower(v2);
        rightFront.setPower(v3);
    }

    @Override
    public double getRawExternalHeading() {
        return imu.getAngularOrientation().firstAngle;
    }

    @Override
    public Double getExternalHeadingVelocity() {
        // TODO: This must be changed to match your configuration
        //                           | Z axis
        //                           |
        //     (Motor Port Side)     |   / X axis
        //                       ____|__/____
        //          Y axis     / *   | /    /|   (IO Side)
        //          _________ /______|/    //      I2C
        //                   /___________ //     Digital
        //                  |____________|/      Analog
        //
        //                 (Servo Port Side)
        //
        // The positive x axis points toward the USB port(s)
        //
        // Adjust the axis rotation rate as necessary
        // Rotate about the z axis is the default assuming your REV Hub/Control Hub is laying
        // flat on a surface

        return (double) imu.getAngularVelocity().zRotationRate;

    }
}
