const Form = JSONSchemaForm.default;

class CostumFormComponent extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return ( <
      Form id = {
        this.props.id
      }
      className = {
        this.props.className
      }
      name = {
        this.props.name
      }
      schema = {
        this.props.schema
      }
      uiSchema = {
        this.props.uiSchema
      }
      formData = {
        this.props.formData
      }
      onSubmit = {
        this.props.onSubmit
      } >
      <
      div >
      <
      button type = "submit"
      class = "btn btn-primary" > {
        this.props.btnText
      } < /button> <
      /div> <
      /Form>
    )
  }
}