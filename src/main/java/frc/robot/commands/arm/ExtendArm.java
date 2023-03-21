// // Copyright (c) 2023 FRC Team 2881 - The Lady Cans
// //
// // Open Source Software; you can modify and/or share it under the terms of BSD
// // license file in the root directory of this project.

package frc.robot.commands.arm;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.PIDCommand;
import frc.robot.subsystems.Arm;

public class ExtendArm extends PIDCommand {
    private Arm m_arm;
   /** Creates a new Lower. */
   public ExtendArm(Arm arm, double position) {
     // Use addRequirements() here to declare subsystem dependencies.
     super(new PIDController(3, 0, 1.5),
      () -> arm.getExtensionEncoder().getPosition(),
      position,
      output -> arm.moveExtensionArm(output),
      arm
     );

     m_arm = arm;
   }

   // Called once the command ends or is interrupted.
   @Override
   public void end(boolean interrupted) {
     m_arm.setExtendedBoolean(true);
     m_arm.moveExtensionArm(0);
   }

   // Returns true when the command should end.
   @Override
   public boolean isFinished() {
     return getController().atSetpoint();
   }
 }
