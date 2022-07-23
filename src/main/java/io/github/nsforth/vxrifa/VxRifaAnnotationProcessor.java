/*
 * Copyright (C) 2017 Nikita Staroverov.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package io.github.nsforth.vxrifa;

import com.squareup.javapoet.JavaFile;
import io.github.nsforth.vxrifa.annotations.VxRifa;
import io.github.nsforth.vxrifa.annotations.VxRifaPublish;
import io.github.nsforth.vxrifa.generators.PublisherGenerator;
import io.github.nsforth.vxrifa.generators.ReceiverGenerator;
import io.github.nsforth.vxrifa.generators.SenderGenerator;
import io.github.nsforth.vxrifa.util.GeneratorsHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * 
 * @author Nikita Staroverov
 */
public class VxRifaAnnotationProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private Elements elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {

        super.init(processingEnv);

        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elements = processingEnv.getElementUtils();

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        Set<? extends Element> senders_elements = roundEnv.getElementsAnnotatedWith(VxRifa.class);
        Set<? extends Element> publishers_elements = roundEnv.getElementsAnnotatedWith(VxRifaPublish.class);

        if (senders_elements.isEmpty() && publishers_elements.isEmpty()) {

            return false;

        }

        for (Element element : senders_elements) {

            if (isNotInterfaceElement(element)) continue;
            
            TypeElement interfaceElement = (TypeElement) element;
            PackageElement packageElement = (PackageElement) interfaceElement.getEnclosingElement();

            if (hasNoMethodsAtAll(interfaceElement)) continue;
            
            try {

                generateSender(interfaceElement, packageElement);                
                generateReceiver(interfaceElement, packageElement);            

            } catch (IOException ex) {
                Logger.getLogger(VxRifaAnnotationProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for (Element element : publishers_elements) {
            
            if (isNotInterfaceElement(element)) continue;
            
            TypeElement interfaceElement = (TypeElement) element;
            PackageElement packageElement = (PackageElement) interfaceElement.getEnclosingElement();

            if (hasNoMethodsAtAll(interfaceElement)) continue;
            
            try {

                generatePublisher(interfaceElement, packageElement);                
                generateReceiver(interfaceElement, packageElement);            

            } catch (IOException ex) {
                Logger.getLogger(VxRifaAnnotationProcessor.class.getName()).log(Level.SEVERE, null, ex); //TODO Заменить на консольный вывод ошибки компиляции
            }

        }

        return true;

    }

    private boolean isNotInterfaceElement(Element element) {
        
        if (element.getKind() != ElementKind.INTERFACE) {
            messager.printMessage(Diagnostic.Kind.ERROR, "This annotation can be applied only to interfaces!", element, element.getAnnotationMirrors().get(0));
            return true;
        }
        
        return false;
    
    }
    
    private boolean hasNoMethodsAtAll(TypeElement interfaceElement) {
        for (Element enclosedElement : elements.getAllMembers(interfaceElement)) {
            if (GeneratorsHelper.isElementSuitableMethod(enclosedElement)) {
                return false;
            }
        }
        return true;
    }

    private void generateSender(TypeElement interfaceElement, PackageElement packageElement) throws IOException {
        
        SenderGenerator senderGenerator = new SenderGenerator(messager, interfaceElement, elements)
                .generateInitializing()
                .generateMethods()
                .generateHandler();
        
        JavaFile senderFile = JavaFile.builder(packageElement.getQualifiedName().toString(), senderGenerator.buildClass()).build();
        
        senderFile.writeTo(filer);
    
    }
    
    private void generatePublisher(TypeElement interfaceElement, PackageElement packageElement) throws IOException {
        
        PublisherGenerator publisherGenerator = new PublisherGenerator(messager, interfaceElement, elements)
                .generateInitializing()
                .generateMethods();
        
        JavaFile publisherFile = JavaFile.builder(packageElement.getQualifiedName().toString(), publisherGenerator.buildClass()).build();
        
        publisherFile.writeTo(filer);
    
    }

    private void generateReceiver(TypeElement interfaceElement, PackageElement packageElement) throws IOException {
        
        ReceiverGenerator receiverGenerator = new ReceiverGenerator(messager, interfaceElement, elements)
                .generateInitializing()
                .generateRegisterMethod()
                .generateUnregisterMethod();
        
        JavaFile receiverFile = JavaFile.builder(packageElement.getQualifiedName().toString(), receiverGenerator.buildClass()).build();
        
        receiverFile.writeTo(filer);
    
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        HashSet<String> annotations = new HashSet<>();

        annotations.add(VxRifa.class.getCanonicalName());
        annotations.add(VxRifaPublish.class.getCanonicalName());

        return Collections.unmodifiableSet(annotations);

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {

        return SourceVersion.latestSupported();

    }

}
