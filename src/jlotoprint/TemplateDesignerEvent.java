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
public class TemplateDesignerEvent extends Event{
	
    public static final EventType<TemplateDesignerEvent> CLOSE = new EventType(ANY, "CLOSE_CONFIRMED");
	public static final EventType<TemplateDesignerEvent> CANCEL = new EventType(ANY, "CANCEL");
    
    public TemplateDesignerEvent(EventType<TemplateDesignerEvent> eventType) {
        super(eventType);
    }

    public TemplateDesignerEvent(Object obj, EventTarget eventTarget, EventType<TemplateDesignerEvent> eventType) {
        super(obj, eventTarget, eventType);
    }  
}
