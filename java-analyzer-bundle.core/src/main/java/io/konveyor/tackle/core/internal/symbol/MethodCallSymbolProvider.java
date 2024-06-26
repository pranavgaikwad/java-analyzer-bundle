package io.konveyor.tackle.core.internal.symbol;

import static org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin.logInfo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;

public class MethodCallSymbolProvider implements SymbolProvider, WithQuery {
    private String query;
    
    @Override
    public List<SymbolInformation> get(SearchMatch match) {
        SymbolKind k = convertSymbolKind((IJavaElement) match.getElement());
        List<SymbolInformation> symbols = new ArrayList<>();
        // For Method Calls we will need to do the local variable trick
        try {
            MethodReferenceMatch m = (MethodReferenceMatch) match;
            IMethod e = (IMethod) m.getElement();
            SymbolInformation symbol = new SymbolInformation();
            symbol.setName(e.getElementName());
            symbol.setKind(convertSymbolKind(e));
            symbol.setContainerName(e.getParent().getElementName());
            symbol.setLocation(getLocation(e, match));
            if (this.query.contains(".")) {
                ICompilationUnit unit = e.getCompilationUnit();
                ASTParser astParser = ASTParser.newParser(AST.getJLSLatest());
                astParser.setSource(unit);
                astParser.setResolveBindings(true);
                CompilationUnit cu = (CompilationUnit) astParser.createAST(null);
                CustomASTVisitor visitor = new CustomASTVisitor(query, match);
                cu.accept(visitor);
                if (visitor.symbolMatches()) {
                    symbols.add(symbol);
                }
            } else {
                symbols.add(symbol);
            }
        } catch (Exception e) {
            logInfo("unable to convert for variable: " + e);
        }

        return symbols;
    }
    @Override
    public void setQuery(String query) {
        this.query = query;
    }
}
