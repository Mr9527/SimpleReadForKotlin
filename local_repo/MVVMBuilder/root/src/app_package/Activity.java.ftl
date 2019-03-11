<#import "root://activities/MVVMBuilder/globals.xml.ftl" as gb>
<#assign activityName>${pageName}Activity</#assign>
<#assign dataBindingName>${gb.getName(activityLayoutName)}Binding</#assign>
package ${ativityPackageName};

import javax.inject.Inject;
import com.rank.basiclib.di.Injectable;
import com.rank.basiclib.ext.CompatActivity;
import com.rank.binddepend_annotation.BindDepend;
import ${packageName}.databinding.${dataBindingName};
import com.rank.basiclib.Constant.ClassType.ACTIVITY;
<#if needRouter>
import com.alibaba.android.arouter.facade.annotation.Route;
</#if>
<#if (needModel)>
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import ${modelPackageName}.${viewModelClass}
</#if>
import ${packageName}.R;

<@gb.fileHeader />
<#if needRouter>
@Route(path = "${RouterName}")
</#if>
@BindDepend(ACTIVITY)
public class  ${activityClass}  extends CompatActivity<${dataBindingName}> implements Injectable {

<#if (needModel)>
    @Inject
    ViewModelProvider.Factory viewModelFactory;
</#if>
    ${viewModelClass} viewModel;

    @Override
    public int getLayoutId() {
        return  R.layout.${activityLayoutName};
    }

    @Override
    public void initViews() {
        <#if (needModel)>
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(${viewModelClass}.class);
        </#if>
    }

    @Override
    public void initEvents() {

    }
}