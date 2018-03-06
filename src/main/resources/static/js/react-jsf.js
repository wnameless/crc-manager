const Form = JSONSchemaForm.default;

class CostumFormComponent extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<Form id={this.props.id}
	        	  name={this.props.name}
	        	  schema={this.props.schema}
	        	  uiSchema={this.props.uiSchema}
	        	  formData={this.props.formData}
	        	  onSubmit={this.props.onSubmit} >
	        </Form>
		)
	}
}

class FormComponent extends React.Component {
   render() {
        return (
        	<Form id='reactForm'
        		  name='reactForm'
        		  schema={schema}
        	      uiSchema={uiSchema}
        	      formData={formData}
        	      onSubmit={onSubmit} >
        	</Form>
        )
    }
}

class ReadOnlyFormComponent extends React.Component {
	render() {
		return (
			<Form id='reactForm'
	        		  name='reactForm'
	        		  noValidate='true'
	        		  schema={schema}
	        	    uiSchema={uiSchema}
	        	    formData={formData}
	        	    onSubmit={onSubmit} >
	        	<div>
	            	<button type="submit" class="btn btn-primary">Back</button>
	            </div>
	        </Form>
		)
	}
}

class NoValidationFormComponent extends React.Component {
	render() {
		return (
			<Form id='reactForm'
	        		  name='reactForm'
	        		  noValidate='true'
	        		  schema={schema}
	        	      uiSchema={uiSchema}
	        	      formData={formData}
	        	      onSubmit={onSubmit} >
	        	<div>
	            	<button type="submit" class="btn btn-primary">Save</button>
	            </div>
	        </Form>
		)
	}
}