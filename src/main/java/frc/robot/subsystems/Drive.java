// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.utils.NavX;
import frc.robot.utils.SwerveModule;

public class Drive extends SubsystemBase {
  // Create SwerveModules
  private final SwerveModule m_frontLeft = new SwerveModule(
      Constants.Drive.kFrontLeftDrivingCanId,
      Constants.Drive.kFrontLeftTurningCanId,
      Constants.Drive.kFrontLeftCanCoderId,
      Constants.Drive.kFrontLeftChassisAngularOffset);

  private final SwerveModule m_frontRight = new  SwerveModule(
      Constants.Drive.kFrontRightDrivingCanId,
      Constants.Drive.kFrontRightTurningCanId,
      Constants.Drive.kFrontRightCanCoderId, 
      Constants.Drive.kFrontRightChassisAngularOffset);

  private final SwerveModule m_rearLeft = new  SwerveModule(
      Constants.Drive.kRearLeftDrivingCanId,
      Constants.Drive.kRearLeftTurningCanId,
      Constants.Drive.kRearLeftCanCoderId,
      Constants.Drive.kBackLeftChassisAngularOffset);

  private final SwerveModule m_rearRight = new  SwerveModule(
      Constants.Drive.kRearRightDrivingCanId,
      Constants.Drive.kRearRightTurningCanId,
      Constants.Drive.kRearRightCanCoderId, 
      Constants.Drive.kBackRightChassisAngularOffset);

  // The gyro sensor
  private final NavX m_gyro = new NavX();

  // Odometry class for tracking robot pose
  SwerveDriveOdometry m_odometry = new SwerveDriveOdometry(
      Constants.Drive.kDriveKinematics,
      Rotation2d.fromDegrees(m_gyro.getAngle()),
      new SwerveModulePosition[] {
          m_frontLeft.getPosition(),
          m_frontRight.getPosition(),
          m_rearLeft.getPosition(),
          m_rearRight.getPosition()
      });

  /** Creates a new DriveSubsystem. */
  public Drive() {
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Drive/Swerve/FrontLeft/SteeringRelativePosition", m_frontLeft.getSteeringRelativePosition());
    SmartDashboard.putNumber("Drive/Swerve/FrontLeft/SteeringAbsolutePosition", m_frontLeft.getSteeringAbsolutePosition());
    SmartDashboard.putNumber("Drive/Swerve/FrontRight/SteeringRelativePosition", m_frontRight.getSteeringRelativePosition());
    SmartDashboard.putNumber("Drive/Swerve/FrontRight/SteeringAbsolutePosition", m_frontRight.getSteeringAbsolutePosition());
    SmartDashboard.putNumber("Drive/Swerve/RearLeft/SteeringRelativePosition", m_rearLeft.getSteeringRelativePosition());
    SmartDashboard.putNumber("Drive/Swerve/RearLeft/SteeringAbsolutePosition", m_rearLeft.getSteeringAbsolutePosition());
    SmartDashboard.putNumber("Drive/Swerve/RearRight/SteeringRelativePosition", m_rearRight.getSteeringRelativePosition());
    SmartDashboard.putNumber("Drive/Swerve/RearRight/SteeringAbsolutePosition", m_rearRight.getSteeringAbsolutePosition());
    SmartDashboard.putNumber("Drive/NavX/Angle", m_gyro.getAngle());
    SmartDashboard.putNumber("Drive/NavX/Yaw", m_gyro.getYaw());
    
    // Update the odometry in the periodic block
    m_odometry.update(
        Rotation2d.fromDegrees(m_gyro.getAngle()),
        new SwerveModulePosition[] {
            m_frontLeft.getPosition(),
            m_frontRight.getPosition(),
            m_rearLeft.getPosition(),
            m_rearRight.getPosition()
        });

  }

  /**
   * Returns the currently-estimated pose of the robot.
   *
   * @return The pose.
   */
  public Pose2d getPose() {
    return m_odometry.getPoseMeters();
  }

  /**
   * Resets the odometry to the specified pose.
   *
   * @param pose The pose to which to set the odometry.
   */
  public void resetOdometry(Pose2d pose) {
    m_odometry.resetPosition(
        Rotation2d.fromDegrees(m_gyro.getAngle()),
        new SwerveModulePosition[] {
            m_frontLeft.getPosition(),
            m_frontRight.getPosition(),
            m_rearLeft.getPosition(),
            m_rearRight.getPosition()
        },
        pose);
  }
  

  /**
   * Method to drive the robot using joystick info.
   *
   * @param xSpeed        Speed of the robot in the x direction (forward).
   * @param ySpeed        Speed of the robot in the y direction (sideways).
   * @param rot           Angular rate of the robot.
   * @param fieldRelative Whether the provided x and y speeds are relative to the
   *                      field.
   */
  public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
    // Adjust input based on   speed
    xSpeed *= Constants.Drive.kMaxSpeedMetersPerSecond;
    ySpeed *= Constants.Drive.kMaxSpeedMetersPerSecond;
    rot *= Constants.Drive.kMaxAngularSpeed;

    var swerveModuleStates = Constants.Drive.kDriveKinematics.toSwerveModuleStates(
        fieldRelative
            ? ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, rot, Rotation2d.fromDegrees(m_gyro.getAngle()))
            : new ChassisSpeeds(xSpeed, ySpeed, rot));
    SwerveDriveKinematics.desaturateWheelSpeeds(
        swerveModuleStates, Constants.Drive.kMaxSpeedMetersPerSecond);
    m_frontLeft.setDesiredState(swerveModuleStates[0]);
    m_frontRight.setDesiredState(swerveModuleStates[1]);
    m_rearLeft.setDesiredState(swerveModuleStates[2]);
    m_rearRight.setDesiredState(swerveModuleStates[3]);
  }

  /**
   * Sets the wheels into an X formation to prevent movement.
   */
  public void setX() {
    m_frontLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(45)));
    m_frontRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-45)));
    m_rearLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-45)));
    m_rearRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(45)));
  }

  /**
   * Sets the swerve ModuleStates.
   *
   * @param desiredStates The desired SwerveModule states.
   */
  public void setModuleStates(SwerveModuleState[] desiredStates) {
    SwerveDriveKinematics.desaturateWheelSpeeds(
        desiredStates, Constants.Drive.kMaxSpeedMetersPerSecond);
    m_frontLeft.setDesiredState(desiredStates[0]);
    m_frontRight.setDesiredState(desiredStates[1]);
    m_rearLeft.setDesiredState(desiredStates[2]);
    m_rearRight.setDesiredState(desiredStates[3]);
  }

  /** Resets the drive encoders to currently read a position of 0. */
  public void resetEncoders() {
    m_frontLeft.resetEncoders();
    m_rearLeft.resetEncoders();
    m_frontRight.resetEncoders();
    m_rearRight.resetEncoders();
  }

  /** Zeroes the heading of the robot. */
  public void zeroHeading() {
    m_gyro.reset();
  }

  /**
   * Returns the heading of the robot.
   *
   * @return the robot's heading in degrees, from -180 to 180
   */
  public double getHeading() {
    return Rotation2d.fromDegrees(m_gyro.getAngle()).getDegrees();
  }

  /**
   * Returns the turn rate of the robot.
   *
   * @return The turn rate of the robot, in degrees per second
   */
  public double getTurnRate() {
    return m_gyro.getRate() * (Constants.Drive.kGyroReversed ? -1.0 : 1.0);
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    super.initSendable(builder);
    m_gyro.initSendable(builder);
  }
}