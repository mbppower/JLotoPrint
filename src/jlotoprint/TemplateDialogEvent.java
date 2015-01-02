/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.event.Event;

/**
 *
 * @author Marcel.Barbosa
 */
public class TemplateDialogEvent extends Event{
	
    public static final EventType<TemplateDialogEvent> SELECTED = new EventType(ANY, "SELECTED");
	public static final EventType<TemplateDialogEvent> CANCELED = new EventType(ANY, "CANCELED");
    
    public TemplateDialogEvent(EventType<TemplateDialogEvent> eventType) {
        super(eventType);
    }

    public TemplateDialogEvent(Object obj, EventTarget eventTarget, EventType<TemplateDialogEvent> eventType) {
        super(obj, eventTarget, eventType);
    }  
}
