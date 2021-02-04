package myExtension;

import java.io.IOException;
import java.util.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import yaskawa.ext.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import yaskawa.ext.api.PendantEvent;
import yaskawa.ext.api.PendantEventType;
import yaskawa.ext.api.Position;
import yaskawa.ext.api.PredefinedCoordFrameType;
import yaskawa.ext.api.Robot;
import yaskawa.ext.api.Scope;
import yaskawa.ext.api.VariableAddress;
import yaskawa.ext.api.AddressSpace;
import yaskawa.ext.api.Any;
import yaskawa.ext.api.ControllerEvent;
import yaskawa.ext.api.ControllerEventType;
import yaskawa.ext.api.CoordFrameRepresentation;
import yaskawa.ext.api.CoordinateFrame;
import yaskawa.ext.api.OrientationUnit;

public class MyExtension {
	
	public MyExtension() throws TTransportException, Exception{
		
		var myExtVersion = new Version(1,0,0);
		var languages = Set.of("en");
		
//		extension = new Extension("dev.visumatic",
//									myExtVersion,
//									"Acme Me",
//									languages,
//									"Localhost", 10080);

		extension = new Extension("dev.visumatic",
				myExtVersion,
				"Acme Me",
				languages,
				"10.7.3.47", 20080);
		
		pendant = extension.pendant();
		controller = extension.controller();
	}
	
	protected Extension extension;
	protected Pendant pendant;
	protected Controller controller;
	protected Robot robot;
	
	int inBit = 5;
	int inFeed = 6;
	int inTool = 7;
	int inPass = 8;
	int otBit = 5;
	int otFeed = 6;
	int otTool = 7;
	int timeMaxDrive = 3;
	int timeMaxFeed = 3;
	int timeBlowScrew = 200;
	int positionApproach = 21;
	int positionDrive = 22;
	int positionDepart = 21;
	boolean same = true;
	int toolNumber = 1;
	
	public void run() throws TException, IOException{
		
		System.out.println("API version: " + extension.apiVersion());
		extension.info("Hello Smart Pendant, I'm MyExtension");
		
        String yml = new String(Files.readAllBytes(Paths.get("MyUtility.yml")), StandardCharsets.UTF_8);
        var errors = pendant.registerYML(yml);
        
        pendant.registerImageFile("C:\\Users\\jiaji\\Desktop\\LeetCode Learn\\MyExtension\\res\\test.PNG");
        pendant.registerUtilityWindow("myutil", "MyUtility",
                                      "Visumatic", "Visumatic Screwdriver Setting");
        
        pendant.addItemEventConsumer("inTool", PendantEventType.Activated, this::onActivatedInTool);
        pendant.addItemEventConsumer("inFeed", PendantEventType.Activated, this::onActivatedInFeed);
        pendant.addItemEventConsumer("inBit", PendantEventType.Activated, this::onActivatedInBit);
        pendant.addItemEventConsumer("inPass", PendantEventType.Activated, this::onActivatedInPass);
        pendant.addItemEventConsumer("otTool", PendantEventType.Activated, this::onActivatedOtTool);
        pendant.addItemEventConsumer("otFeed", PendantEventType.Activated, this::onActivatedOtFeed);
        pendant.addItemEventConsumer("otBit", PendantEventType.Activated, this::onActivatedOtBit);
        
        pendant.addItemEventConsumer("timeMaxDrive", PendantEventType.EditingFinished, this::onEditedTimeDrive);
        pendant.addItemEventConsumer("timeMaxFeed", PendantEventType.EditingFinished, this::onEditedTimeFeed);
        pendant.addItemEventConsumer("timeBlowScrew", PendantEventType.EditingFinished, this::onEditedTimeBlow);
        pendant.addItemEventConsumer("toolNumber", PendantEventType.EditingFinished, this::onEditedToolNumber);
        pendant.addItemEventConsumer("positionApproach", PendantEventType.EditingFinished, this::onEditedPositionApproach);
        pendant.addItemEventConsumer("positionDrive", PendantEventType.EditingFinished, this::onEditedPositionDrive);
        pendant.addItemEventConsumer("positionDepart", PendantEventType.EditingFinished, this::onEditedPositionDepart);
        
        pendant.addItemEventConsumer("checkBox", PendantEventType.CheckedChanged, this::onChecked);

        pendant.addItemEventConsumer("tab1", PendantEventType.Clicked, this::onClickedTab1);
        pendant.addItemEventConsumer("tab2", PendantEventType.Clicked, this::onClickedTab2);
        pendant.addItemEventConsumer("tab3", PendantEventType.Clicked, this::onClickedTab3);
        
        pendant.addItemEventConsumer("bnPurge", PendantEventType.Clicked, this::onClickedBnPurge);
        pendant.addItemEventConsumer("bnFeed", PendantEventType.Clicked, this::onClickedBnFeed);
        pendant.addItemEventConsumer("bnInsertFeed", PendantEventType.Clicked, this::onClickedBnInsertFeed);
        pendant.addItemEventConsumer("bnBit", PendantEventType.Clicked, this::onClickedBnBit);
        pendant.addItemEventConsumer("bnTool", PendantEventType.Clicked, this::onClickedBnTool);
        pendant.addItemEventConsumer("bnInsertDrive", PendantEventType.Clicked, this::onClickedBnInsertDrive);
        pendant.addItemEventConsumer("bnPositionApproach", PendantEventType.Clicked, this::onClickedBnPositionApproach);
        pendant.addItemEventConsumer("bnPositionDrive", PendantEventType.Clicked, this::onClickedBnPositionDrive);
        pendant.addItemEventConsumer("bnPositionDepart", PendantEventType.Clicked, this::onClickedBnPositionDepart);
        pendant.addItemEventConsumer("bnInsertAllJobs", PendantEventType.Clicked, this::onClickedBnInsertAllJobs);
        
        controller.subscribeEventTypes(Set.of(ControllerEventType.IOValueChanged));
        controller.monitorOutputGroups(1, 1);
        controller.monitorInputGroups(1, 1);
        controller.addEventConsumer(ControllerEventType.IOValueChanged, this::onIoValueChanged);
        
        extension.outputEvents = true;
        extension.run(() -> false);
        
	}
	
	public static void main(String[] args) {
		try {
			MyExtension myExtension = new MyExtension();
			myExtension.run();
		} catch (Exception e) {
			System.out.println("Exception: " + e.toString());
		}
	}
	
	// Click Tab to Update Monitor
	
	void onClickedTab1(PendantEvent e) {
		try {
			pendant.setProperty("txtInsertFeed", "text", " ");
			pendant.setProperty("txtInsertDrive", "text", " ");
		} catch (Exception ex) {
			System.out.println("Unable to:"+ ex.getMessage());
		}
	}
	
	void onClickedTab2(PendantEvent e) {
		try {
			if (controller.inputValue(inFeed) == true) {
				pendant.setProperty("mtInFeed", "color", "light green");
			} else {
				pendant.setProperty("mtInFeed", "color", "white");
			}
			
			if (controller.inputValue(inBit) == true) {
				pendant.setProperty("mtInBit", "color", "light green");
			} else {
				pendant.setProperty("mtInBit", "color", "white");
			}
			
			if (controller.outputValue(otFeed) == true) {
				pendant.setProperty("mtOtFeed", "color", "light green");
				pendant.setProperty("bnFeed", "text", "Feed Screw Off");
			} else {
				pendant.setProperty("mtOtFeed", "color", "white");
				pendant.setProperty("bnFeed", "text", "Feed Screw On");
			}
			
			if (controller.outputValue(otBit) == true) {
				pendant.setProperty("mtOtBit", "color", "light green");
			} else {
				pendant.setProperty("mtOtBit", "color", "white");
			}
			
		} catch (Exception ex) {
			System.out.println("Unable to:"+ ex.getMessage());
		}
	}
	
	void onClickedTab3(PendantEvent e) {
		try {
			
			if (controller.inputValue(inTool) == true) {
				pendant.setProperty("mtInTool", "color", "light green");
			} else {
				pendant.setProperty("mtInTool", "color", "white");
			}
			
			if (controller.inputValue(inFeed) == true) {
				pendant.setProperty("mtInFeed2", "color", "light green");
			} else {
				pendant.setProperty("mtInFeed2", "color", "white");
			}
			
			if (controller.inputValue(inBit) == true) {
				pendant.setProperty("mtInBit2", "color", "light green");
			} else {
				pendant.setProperty("mtInBit2", "color", "white");
			}
			
			if (controller.inputValue(inPass) == true) {
				pendant.setProperty("mtInPass", "color", "light green");
			} else {
				pendant.setProperty("mtInPass", "color", "white");
			}
			
			if (controller.outputValue(otTool) == true) {
				pendant.setProperty("mtOtTool", "color", "light green");
				pendant.setProperty("bnTool", "text", "Drive Screw Off");
			} else {
				pendant.setProperty("mtOtTool", "color", "white");
				pendant.setProperty("bnTool", "text", "Drive Screw On");
			}
			
			if (controller.outputValue(otBit) == true) {
				pendant.setProperty("mtOtBit2", "color", "light green");
				pendant.setProperty("bnBit", "text", "Retract Bit");
			} else {
				pendant.setProperty("mtOtBit2", "color", "white");
				pendant.setProperty("bnBit", "text", "Extend Bit");
			}
			
		} catch (Exception ex) {
			System.out.println("Unable to:"+ ex.getMessage());
		}
	}
	
	// IO Select
	void onActivatedOtTool(PendantEvent e) {
		try {
			otTool = (int) (pendant.property("otTool", "currentIndex").getIValue() + 2);
			pendant.setProperty("txtOtTool", "text", "Output#" + otTool);
		} catch (Exception ex) {
			System.out.println("Unable to set Output for Start Tool:"+ ex.getMessage());
		}
	}
	
	void onActivatedOtFeed(PendantEvent e) {
		try {
			otFeed = (int) (pendant.property("otFeed", "currentIndex").getIValue() + 2);
			pendant.setProperty("txtOtFeed", "text", "Output#" + otFeed);
		} catch (Exception ex) {
			System.out.println("Unable to set Output for Blow Screw:"+ ex.getMessage());
		}
	}
	
	void onActivatedOtBit(PendantEvent e) {
		try {
			otBit = (int) (pendant.property("otBit", "currentIndex").getIValue() + 2);
			pendant.setProperty("txtOtBit", "text", "Output#" + otBit);
			pendant.setProperty("txtOtBit2", "text", "Output#" + otBit);
		} catch (Exception ex) {
			System.out.println("Unable to set Output for Extend Bit:"+ ex.getMessage());
		}
	}
	
	void onActivatedInTool(PendantEvent e) {
		try {
			inTool = (int) (pendant.property("inTool", "currentIndex").getIValue() + 4);
			pendant.setProperty("txtInTool", "text", "Input#" + inTool);
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Running:"+ ex.getMessage());
		}
	}
	
	void onActivatedInFeed(PendantEvent e) {
		try {
			inFeed = (int) (pendant.property("inFeed", "currentIndex").getIValue() + 4);
			pendant.setProperty("txtInFeed", "text", "Input#" + inFeed);
			pendant.setProperty("txtInFeed2", "text", "Input#" + inFeed);
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Feed Sensor:"+ ex.getMessage());
		}
	}
	
	void onActivatedInBit(PendantEvent e) {
		try {
			inBit = (int) (pendant.property("inBit", "currentIndex").getIValue() + 4);
			pendant.setProperty("txtInBit", "text", "Input#" + inBit);
			pendant.setProperty("txtInBit2", "text", "Input#" + inBit);
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Bit Retracted:"+ ex.getMessage());
		}
	}
	
	void onActivatedInPass(PendantEvent e) {
		try {
			inPass = (int) (pendant.property("inPass", "currentIndex").getIValue() + 4);
			pendant.setProperty("txtInPass", "text", "Input#" + inPass);
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	// Parameter Set
	void onEditedTimeDrive(PendantEvent e) {
		try {
			String timeDrive = (pendant.property("timeMaxDrive", "text").getSValue());
			timeMaxDrive = Integer.parseInt(timeDrive);
			if (timeMaxDrive < 1) {
				timeMaxDrive = 1;
				pendant.setProperty("timeMaxDrive", "text", timeMaxDrive);
			} else if (timeMaxDrive > 10) {
				timeMaxDrive = 10;
				pendant.setProperty("timeMaxDrive", "text", timeMaxDrive);
			}
			System.out.println(timeMaxDrive);
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	void onEditedTimeFeed(PendantEvent e) {
		try {
			String timeFeed = (pendant.property("timeMaxFeed", "text").getSValue());
			timeMaxFeed = Integer.parseInt(timeFeed);
			if (timeMaxFeed < 1) {
				timeMaxFeed = 1;
				pendant.setProperty("timeMaxFeed", "text", timeMaxFeed);
			} else if (timeMaxFeed > 10) {
				timeMaxFeed = 10;
				pendant.setProperty("timeMaxFeed", "text", timeMaxFeed);
			}
			System.out.println(timeMaxFeed);
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	void onEditedTimeBlow(PendantEvent e) {
		try {
			String timeBlow = (pendant.property("timeBlowScrew", "text").getSValue());
			timeBlowScrew = Integer.parseInt(timeBlow);
			if (timeBlowScrew < 50) {
				timeBlowScrew = 50;
				pendant.setProperty("timeBlowScrew", "text", timeBlowScrew);
			} else if (timeBlowScrew > 500) {
				timeBlowScrew = 500;
				pendant.setProperty("timeBlowScrew", "text", timeBlowScrew);
			}
			System.out.println(timeBlowScrew);
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	void onEditedToolNumber(PendantEvent e) {
		try {
			String toolNumberString = (pendant.property("toolNumber", "text").getSValue());
			toolNumber = Integer.parseInt(toolNumberString);
			if (toolNumber < 0) {
				toolNumber = 0;
				pendant.setProperty("toolNumber", "text", toolNumber);
			} else if (toolNumber > 63) {
				toolNumber = 63;
				pendant.setProperty("toolNumber", "text", toolNumber);
			}
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	void onEditedPositionApproach(PendantEvent e) {
		try {
			String positionApproachString = (pendant.property("positionApproach", "text").getSValue());
			positionApproach = Integer.parseInt(positionApproachString);
			if (positionApproach < 0) {
				positionApproach = 0;
				pendant.setProperty("positionApproach", "text", positionApproach);
			} else if (positionApproach > 127) {
				positionApproach = 127;
				pendant.setProperty("positionApproach", "text", positionApproach);
			}
			
			pendant.setProperty("txtPositionApproach", "text", "");
			
			if(pendant.property("checkBox", "checked").getBValue()) {
				positionDepart = positionApproach;
				pendant.setProperty("positionDepart", "text", positionDepart);
				pendant.setProperty("txtPositionDepart", "text", "");
			}
			
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	void onEditedPositionDrive(PendantEvent e) {
		try {
			String positionDriveString = (pendant.property("positionDrive", "text").getSValue());
			positionDrive = Integer.parseInt(positionDriveString);
			if (positionDrive < 0) {
				positionDrive = 0;
				pendant.setProperty("positionDrive", "text", positionDrive);
			} else if (positionDrive > 127) {
				positionDrive = 127;
				pendant.setProperty("positionDrive", "text", positionDrive);
			}
			
			pendant.setProperty("txtPositionDrive", "text", "");
			
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	void onEditedPositionDepart(PendantEvent e) {
		try {
			String positionDepartString = (pendant.property("positionDepart", "text").getSValue());
			positionDepart = Integer.parseInt(positionDepartString);
			if (positionDepart < 0) {
				positionDepart = 0;
				pendant.setProperty("positionDepart", "text", positionDepart);
			} else if (positionDepart > 127) {
				positionDepart = 127;
				pendant.setProperty("positionDepart", "text", positionDepart);
			}
			
			pendant.setProperty("txtPositionDepart", "text", "");
			
			if (positionDepart != positionApproach) {
				pendant.setProperty("checkBox", "checked", false);
			}
			
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	// Check Set
	void onChecked(PendantEvent e) {
		try {
			
			if (e.getProps().get("checked").getBValue()) {
				positionDepart = positionApproach;
				pendant.setProperty("positionDepart", "text", positionDepart);
				
				if (pendant.property("txtPositionApproach", "text").getSValue().length() != 0) {
					pendant.setProperty("txtPositionDepart", "text", "Saved");
				}
				
			}
			
		} catch (Exception ex) {
			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
		}
	}
	
	// Button Set
	void onClickedBnPurge(PendantEvent e) {
	    
	    try {
	    	controller.setOutput(otBit, true);
	    	controller.setOutput(otBit, false);
	    } catch (Exception ex) {
	        System.out.println("Unable to insert Call Job command: "+ex.getMessage());
	    }
	}
	
	void onClickedBnFeed(PendantEvent e) {
	    
	    try {
	    	
	    	if (controller.outputValue(otFeed)) {
	    		controller.setOutput(otFeed, false);
	    	} else {
	    		controller.setOutput(otFeed, true);
	    	}
	    	
	    } catch (Exception ex) {
	        System.out.println("Unable to insert Call Job command: "+ex.getMessage());
	    }
	}
	
	void onClickedBnInsertFeed(PendantEvent e) {
	    
	    try {
	    	
	    	if (otBit == otFeed || otBit == otTool || otFeed == otTool || inTool == inFeed || inTool == inBit || inTool == inPass || inFeed == inBit || inFeed == inPass || inBit == inPass) {
	    		pendant.setProperty("txtInsertFeed", "text", "Please Check Duplicated IO Number");
	    		return;
	    	}
	    	
	    	String cmd = "CALL JOB:VISUMATIC-FEEDSCREW ("+otBit+", "+otFeed+", "+otTool+", "+inBit+", "+inFeed+", "+timeMaxFeed+", "+timeBlowScrew+")";
	    	pendant.insertInstructionAtSelectedLine(cmd);
	    	
	    } catch (Exception ex) {
	        System.out.println("Unable to insert Call Job command: "+ex.getMessage());
	    }
	}
	
	void onClickedBnBit(PendantEvent e) {
	    
	    try {
	    	
	    	if (controller.outputValue(otBit)) {
	    		controller.setOutput(otBit, false);
	    	} else {
	    		controller.setOutput(otBit, true);
	    	}
	    	
	    } catch (Exception ex) {
	        System.out.println("Unable to insert Call Job command: "+ex.getMessage());
	    }
	}
	
	void onClickedBnTool(PendantEvent e) {
	    
	    try {
	    	
	    	if (controller.outputValue(otTool)) {
	    		controller.setOutput(otTool, false);
	    	} else {
	    		controller.setOutput(otTool, true);
	    	}
	    	
	    } catch (Exception ex) {
	        System.out.println("Unable to insert Call Job command: "+ex.getMessage());
	    }
	}
	
	void onClickedBnInsertDrive(PendantEvent e) {
	    
	    try {
	    	
	    	if (otBit == otFeed || otBit == otTool || otFeed == otTool || inTool == inFeed || inTool == inBit || inTool == inPass || inFeed == inBit || inFeed == inPass || inBit == inPass) {
	    		pendant.setProperty("txtInsertDrive", "text", "Please Check Duplicated IO Number");
	    		return;
	    	}
	    	
	    	String cmd = "CALL JOB:VISUMATIC-DRIVESCREW ("+otBit+", "+otFeed+", "+otTool+", "+inBit+", "+inFeed+", "+inTool+", "+inPass+", "+timeMaxDrive+")";
	    	pendant.insertInstructionAtSelectedLine(cmd);
	    	
	    } catch (Exception ex) {
	        System.out.println("Unable to insert Call Job command: "+ex.getMessage());
	    }
	}
	
	void onClickedBnPositionApproach(PendantEvent e) {
	    
	    try {
	    	
	    	CoordinateFrame frame = new CoordinateFrame(CoordFrameRepresentation.Implicit, PredefinedCoordFrameType.World);
	    	frame.setTool(toolNumber);
	    	Position position = controller.currentRobot().toolTipPosition(frame, toolNumber);
	    	
	    	VariableAddress addr = new VariableAddress(Scope.Global, AddressSpace.Position, positionApproach);

	    	controller.setVariableByAddr(addr, position);
	    	pendant.setProperty("txtPositionApproach", "text", "Saved");
	    	
	    	if (pendant.property("checkBox", "checked").getBValue()) {
	    		pendant.setProperty("txtPositionDepart", "text", "Saved");
	    	}


//	    	System.out.println(controller.currentRobot().jointPosition(OrientationUnit.Degree));
	    	
//	    	controller.setVariable("hello", 666);
//	    	System.out.println(controller.variableAddrByName("hello2"));
	    	
//	    	VariableAddress addr = new VariableAddress(Scope.Global, AddressSpace.Position, 3);
//	    	System.out.println(controller.variableByAddr(addr));
//	    	
//	    	Any pvar = new Any();
//	    	pvar.setPValue(controller.currentRobot().jointPosition(OrientationUnit.Pulse));
    	
//	    	System.out.println(pvar);
//	    	
//	    	System.out.println(controller.variableAddrByName("time1"));
//	    	
//	    	System.out.println(controller.variable("hh2"));
//	    	System.out.println(controller.variable("hh3"));
//	    	
//	    	controller.setVariableByAddr(controller.variableAddrByName("time1"), controller.variable("time2"));
//	    	
//	    	controller.setVariableByAddr(controller.variableAddrByName("hh1"), controller.variable("hh2"));
//	    	
//	    	controller.setVariableByAddr(addr, pvar);
//	    	
	    	
//	    	VariableAddress addr = new VariableAddress(Scope.Global, AddressSpace.Position, 0);
//	    	controller.setVariableByAddr(addr, controller.currentRobot().jointPosition(OrientationUnit.Degree));
	    	
	    } catch (Exception ex) {
	        System.out.println("Wrong Wrong "+ex.getMessage());
	    }
	}
	
	void onClickedBnPositionDrive(PendantEvent e) {
	    
	    try {
	    	
	    	CoordinateFrame frame = new CoordinateFrame(CoordFrameRepresentation.Implicit, PredefinedCoordFrameType.World);
	    	frame.setTool(toolNumber);
	    	Position position = controller.currentRobot().toolTipPosition(frame, toolNumber);
	    	
	    	VariableAddress addr = new VariableAddress(Scope.Global, AddressSpace.Position, positionDrive);

	    	controller.setVariableByAddr(addr, position);
	    	pendant.setProperty("txtPositionDrive", "text", "Saved");
	    	
	    } catch (Exception ex) {
	        System.out.println("Wrong Wrong "+ex.getMessage());
	    }
	}
	
	void onClickedBnPositionDepart(PendantEvent e) {
	    
	    try {
	    	
	    	CoordinateFrame frame = new CoordinateFrame(CoordFrameRepresentation.Implicit, PredefinedCoordFrameType.World);
	    	frame.setTool(toolNumber);
	    	Position position = controller.currentRobot().toolTipPosition(frame, toolNumber);
	    	
	    	VariableAddress addr = new VariableAddress(Scope.Global, AddressSpace.Position, positionDepart);

	    	controller.setVariableByAddr(addr, position);
	    	pendant.setProperty("txtPositionDepart", "text", "Saved");
	    	
	    } catch (Exception ex) {
	        System.out.println("Wrong Wrong "+ex.getMessage());
	    }
	}
	
	void onClickedBnInsertAllJobs(PendantEvent e) {
	    
	    try {
	    	
	    	if (otBit == otFeed || otBit == otTool || otFeed == otTool || inTool == inFeed || inTool == inBit || inTool == inPass || inFeed == inBit || inFeed == inPass || inBit == inPass) {
	    		pendant.setProperty("txtInsertAllJobs", "text", "Please Check Duplicated IO Number");
	    		return;
	    	}
	    	
	    	if (pendant.property("txtPositionApproach", "text").getSValue() != "Saved" || pendant.property("txtPositionDrive", "text").getSValue() != "Saved" && pendant.property("txtPositionDepart", "text").getSValue() != "Saved") {
	    		pendant.setProperty("txtInsertAllJobs", "text", "Please Finish Points Teaching");
	    		return;
	    	}
	    	
	    	String cmd1 = "CALL JOB:VISUMATIC-APPROACHPOSITION ("+positionApproach+")";
	    	pendant.insertInstructionAtSelectedLine(cmd1);
	    	
	    	String cmd2 = "CALL JOB:VISUMATIC-FEEDSCREW ("+otBit+", "+otFeed+", "+otTool+", "+inBit+", "+inFeed+", "+timeMaxFeed+", "+timeBlowScrew+")";
	    	pendant.insertInstructionAtSelectedLine(cmd2);
	    	
	    	String cmd3 = "CALL JOB:VISUMATIC-DRIVEPOSITION ("+positionDrive+")";
	    	pendant.insertInstructionAtSelectedLine(cmd3);
	    	
	    	String cmd4 = "CALL JOB:VISUMATIC-DRIVESCREW ("+otBit+", "+otFeed+", "+otTool+", "+inBit+", "+inFeed+", "+inTool+", "+inPass+", "+timeMaxDrive+")";
	    	pendant.insertInstructionAtSelectedLine(cmd4);
	    	
	    	String cmd5 = "CALL JOB:VISUMATIC-DEPARTPOSITION ("+positionDepart+")";
	    	pendant.insertInstructionAtSelectedLine(cmd5);

	    	
	    } catch (Exception ex) {
	        System.out.println("Wrong Wrong "+ex.getMessage());
	    }
	}
	
	// IO Monitoring
    void onIoValueChanged(ControllerEvent e)
    {  
    	
        if (e.getProps().get("address").getIValue() > 10000 && e.getProps().get("num").getIValue() == otTool && e.getProps().get("value").getBValue() == true) {
    		try {
    			pendant.setProperty("mtOtTool", "color", "light green");
    			pendant.setProperty("bnTool", "text", "Drive Screw Off");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
    	
        if (e.getProps().get("address").getIValue() > 10000 && e.getProps().get("num").getIValue() == otTool && e.getProps().get("value").getBValue() == false) {
    		try {
    			pendant.setProperty("mtOtTool", "color", "white");
    			pendant.setProperty("bnTool", "text", "Drive Screw On");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
        
        if (e.getProps().get("address").getIValue() > 10000 && e.getProps().get("num").getIValue() == otFeed && e.getProps().get("value").getBValue() == true) {
    		try {
    			pendant.setProperty("mtOtFeed", "color", "light green");
    			pendant.setProperty("bnFeed", "text", "Feed Screw Off");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
    	
        if (e.getProps().get("address").getIValue() > 10000 && e.getProps().get("num").getIValue() == otFeed && e.getProps().get("value").getBValue() == false) {
    		try {
    			pendant.setProperty("mtOtFeed", "color", "white");
    			pendant.setProperty("bnFeed", "text", "Feed Screw On");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
        
        if (e.getProps().get("address").getIValue() > 10000 && e.getProps().get("num").getIValue() == otBit && e.getProps().get("value").getBValue() == true) {
    		try {
    			pendant.setProperty("mtOtBit", "color", "light green");
    			pendant.setProperty("mtOtBit2", "color", "light green");
    			pendant.setProperty("bnBit", "text", "Retract Bit");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
    	
        if (e.getProps().get("address").getIValue() > 10000 && e.getProps().get("num").getIValue() == otBit && e.getProps().get("value").getBValue() == false) {
    		try {
    			pendant.setProperty("mtOtBit", "color", "white");
    			pendant.setProperty("mtOtBit2", "color", "white");
    			pendant.setProperty("bnBit", "text", "Extend Bit");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
        
        if (e.getProps().get("address").getIValue() < 10000 && e.getProps().get("num").getIValue() == inTool && e.getProps().get("value").getBValue() == true) {
    		try {
    			pendant.setProperty("mtInTool", "color", "light green");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
    	
        if (e.getProps().get("address").getIValue() < 10000 && e.getProps().get("num").getIValue() == inTool && e.getProps().get("value").getBValue() == false) {
    		try {
    			pendant.setProperty("mtInTool", "color", "white");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
        
        if (e.getProps().get("address").getIValue() < 10000 && e.getProps().get("num").getIValue() == inFeed && e.getProps().get("value").getBValue() == true) {
    		try {
    			pendant.setProperty("mtInFeed", "color", "light green");
    			pendant.setProperty("mtInFeed2", "color", "light green");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
    	
        if (e.getProps().get("address").getIValue() < 10000 && e.getProps().get("num").getIValue() == inFeed && e.getProps().get("value").getBValue() == false) {
    		try {
    			pendant.setProperty("mtInFeed", "color", "white");
    			pendant.setProperty("mtInFeed2", "color", "white");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
        
        if (e.getProps().get("address").getIValue() < 10000 && e.getProps().get("num").getIValue() == inBit && e.getProps().get("value").getBValue() == true) {
    		try {
    			pendant.setProperty("mtInBit", "color", "light green");
    			pendant.setProperty("mtInBit2", "color", "light green");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
    	
        if (e.getProps().get("address").getIValue() < 10000 && e.getProps().get("num").getIValue() == inBit && e.getProps().get("value").getBValue() == false) {
    		try {
    			pendant.setProperty("mtInBit", "color", "white");
    			pendant.setProperty("mtInBit2", "color", "white");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
        
        if (e.getProps().get("address").getIValue() < 10000 && e.getProps().get("num").getIValue() == inPass && e.getProps().get("value").getBValue() == true) {
    		try {
    			pendant.setProperty("mtInPass", "color", "light green");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
    	
        if (e.getProps().get("address").getIValue() < 10000 && e.getProps().get("num").getIValue() == inPass && e.getProps().get("value").getBValue() == false) {
    		try {
    			pendant.setProperty("mtInPass", "color", "white");
    		} catch (Exception ex) {
    			System.out.println("Unable to set Input for Tool Passed:"+ ex.getMessage());
    		}
        }
        
    }
}

