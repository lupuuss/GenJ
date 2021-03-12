package pl.lodz.zzpj.genj;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GenerateObservableAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {



        PsiFile file = e.getDataContext().getData(CommonDataKeys.PSI_FILE);

        if (file instanceof PsiJavaFile) {

            e.getPresentation().setEnabled(true);
            return;
        }

        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiJavaFile file = (PsiJavaFile) e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();

        if (file == null || project == null) {
            return;
        }

        PsiClass[] classes = file.getClasses();

        Optional<PsiClass> psiClassOpt = Arrays.stream(classes)
                .filter(psiCls -> psiCls.getModifierList() != null)
                .filter((psiCls -> psiCls.getModifierList().hasModifierProperty(PsiModifier.PUBLIC)))
                .findFirst();

        if (psiClassOpt.isEmpty()) {
            return;
        }

        PsiClass psiClass = psiClassOpt.get();

        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);

        String actionName = "Event";

        // required to add any text to intelli
        WriteCommandAction.runWriteCommandAction(project, () -> {

            addImportsIfRequired(file, facade);

            List<PsiElement> toAdd = Arrays.asList(
                    createListenerInterface(elementFactory, actionName),
                    createListenerField(elementFactory, project),
                    createListenerMethod(true, psiClass, elementFactory, actionName),
                    createListenerMethod(false, psiClass, elementFactory, actionName)
            );

            PsiElement first = Arrays.stream(psiClass.getChildren())
                    .filter(cls -> cls.textContains('{'))
                    .findFirst()
                    .orElseThrow();

            // reverse provides proper order when adding after first element
            Collections.reverse(toAdd);
            toAdd.forEach(element -> psiClass.addAfter(element, first));
        });
    }

    private PsiMethod createListenerMethod(
            boolean isAdd,
            PsiClass psiClass,
            PsiElementFactory elementFactory,
            String actionName
    ) {

        StringBuilder methodSignature = new StringBuilder();

        methodSignature.append("public void ");

        if (isAdd) {
            methodSignature.append("add");
        } else {
            methodSignature.append("remove");
        }

        methodSignature
                .append("On")
                .append(actionName)
                .append("(Listener listener) {\nlisteners.add(listener);\n}");

        return elementFactory.createMethodFromText(methodSignature.toString(), psiClass);
    }

    private void addImportsIfRequired(PsiJavaFile file, JavaPsiFacade facade) {
        PsiClass listClass = facade.findClass(
                "java.util.List",
                GlobalSearchScope.everythingScope(file.getProject())
        );

        if (listClass == null) {
            return;
        }

        file.importClass(listClass);
    }

    private PsiClass createListenerInterface(@NotNull PsiElementFactory elementFactory, String actionName) {

        PsiClass listenerInterface = elementFactory.createInterface("Listener");

        listenerInterface.add(elementFactory.createMethodFromText("void on" + actionName + "();", listenerInterface));

        return listenerInterface;
    }

    private @NotNull PsiField createListenerField(
            @NotNull PsiElementFactory elementFactory,
            @NotNull Project project
    ) {

        return elementFactory.createField("listeners",
                PsiType.getTypeByName("List<Listener>", project, GlobalSearchScope.allScope(project))
        );
    }
}
